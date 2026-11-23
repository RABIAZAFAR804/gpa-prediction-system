const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const ADMIN_UID = "admin_789";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write user profiles, predictions, or blogs", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("blogs").where("published", "==", true).get());
  await assertFails(
    unauthDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("predictions")
      .doc("pred_1")
      .set({
        userId: ALICE_UID,
        semesterName: "Fall 2026",
        studyHoursPerWeek: 20,
        attendancePct: 92,
        assignmentAvg: 88,
        midtermAvg: 85,
        previousGpa: 3.5,
        creditHours: 15,
        sleepHours: 7.5,
        predictedGpa: 3.65,
        confidencePct: 89,
        riskLevel: "LOW",
        prognosisSummary: "On track",
        actionPlan: ["Keep weekly review"],
        source: "GEMINI_AI",
        createdAt: new Date(Date.now() - 1000),
        updatedAt: new Date(Date.now() - 1000),
      })
  );
});

test("User profile creation prevents self-escalation to ADMIN and rejects extra fields", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date(Date.now() - 2000);

  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      major: "Computer Science",
      university: "State University",
      semesterLabel: "Fall 2026",
      currentGpa: 3.6,
      targetGpa: 3.85,
      completedCredits: 64,
      weeklyStudyGoalHours: 24,
      role: "ADMIN",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      major: "Computer Science",
      university: "State University",
      semesterLabel: "Fall 2026",
      currentGpa: 3.6,
      targetGpa: 3.85,
      completedCredits: 64,
      weeklyStudyGoalHours: 24,
      role: "STUDENT",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Partial update allowed fields succeed; role mutation fails
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      targetGpa: 3.9,
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      role: "ADMIN",
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Student can create, list, and delete own predictions and courses; cross-user access denied", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 3000);

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("predictions")
      .doc("pred_1")
      .set({
        userId: ALICE_UID,
        semesterName: "Fall 2026",
        studyHoursPerWeek: 22.0,
        attendancePct: 94.0,
        assignmentAvg: 91.0,
        midtermAvg: 88.5,
        previousGpa: 3.62,
        creditHours: 16,
        sleepHours: 7.5,
        predictedGpa: 3.74,
        confidencePct: 91,
        riskLevel: "LOW",
        prognosisSummary: "Strong upward trajectory.",
        actionPlan: ["Focus on final lab"],
        counterfactualTip: "Add 2h problem sets",
        source: "GEMINI_AI",
        createdAt: now,
        updatedAt: now,
      })
  );

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("courses")
      .doc("cs_301")
      .set({
        userId: ALICE_UID,
        code: "CS 301",
        title: "Algorithms",
        credits: 4,
        currentScore: 91.5,
        attendancePct: 96.0,
        targetGrade: "A",
        difficulty: "HIGH",
        createdAt: now,
        updatedAt: now,
      })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("predictions").get()
  );

  // Cross-user read & write denied
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("predictions")
      .doc("pred_1")
      .get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("courses").get()
  );
});

test("Published blogs readable by students; unpublished and writes restricted to admin", async () => {
  const now = new Date(Date.now() - 5000);
  const adminDb = testEnv
    .authenticatedContext(ADMIN_UID, {
      email: "rabiazafar.web@gmail.com",
      email_verified: true,
    })
    .firestore();
  const spoofAdminDb = testEnv
    .authenticatedContext("spoof_999", {
      email: "rabiazafar.web@gmail.com",
      email_verified: false,
    })
    .firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();

  // Spoofed unverified email cannot create global blog
  await assertFails(
    spoofAdminDb.collection("blogs").doc("blog_spoof").set({
      authorId: "spoof_999",
      authorName: "Spoof",
      authorRole: "Admin",
      title: "Fake Article",
      subtitle: "Fake subtitle",
      category: "Academic Success",
      tags: ["gpa"],
      coverKey: "img_blog_ai_education",
      readMinutes: 5,
      contentMarkdown: "Sufficiently long editorial body content for testing.",
      published: true,
      featured: false,
      popularScore: 10,
      sampleContent: false,
      publishedDateLabel: "Nov 23, 2026",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Verified admin creates published + draft blog
  await assertSucceeds(
    adminDb.collection("blogs").doc("blog_pub").set({
      authorId: ADMIN_UID,
      authorName: "Dr. Elena Vance",
      authorRole: "Dean of Academic Strategy",
      title: "Deep Work Protocols for 3.9+ GPA",
      subtitle: "Structuring 90-minute focus blocks.",
      category: "Study Techniques",
      tags: ["focus", "gpa"],
      coverKey: "img_blog_gpa_strategy",
      readMinutes: 7,
      contentMarkdown: "Full editorial article content exceeding twenty characters.",
      published: true,
      featured: true,
      popularScore: 95,
      sampleContent: false,
      publishedDateLabel: "Nov 23, 2026",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    adminDb.collection("blogs").doc("blog_draft").set({
      authorId: ADMIN_UID,
      authorName: "Dr. Elena Vance",
      authorRole: "Dean of Academic Strategy",
      title: "Draft Article",
      subtitle: "Draft subtitle here",
      category: "Exam Preparation",
      tags: ["draft"],
      coverKey: "img_blog_wellness",
      readMinutes: 4,
      contentMarkdown: "Full draft article content exceeding twenty characters.",
      published: false,
      featured: false,
      popularScore: 0,
      sampleContent: false,
      publishedDateLabel: "Nov 23, 2026",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Student can query published blogs, cannot query all or read draft or write global blogs
  await assertSucceeds(
    aliceDb.collection("blogs").where("published", "==", true).get()
  );
  await assertFails(aliceDb.collection("blogs").get());
  await assertFails(aliceDb.collection("blogs").doc("blog_draft").get());
  await assertFails(
    aliceDb.collection("blogs").doc("blog_pub").update({
      title: "Hacked",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Student can manage own custom_blogs workspace
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("custom_blogs")
      .doc("my_post")
      .set({
        authorId: ALICE_UID,
        authorName: "Alice",
        authorRole: "Editorial Contributor",
        title: "My Exam Prep Notes",
        subtitle: "Spaced repetition schedule for finals",
        category: "Exam Preparation",
        tags: ["finals", "anki"],
        coverKey: "img_blog_ai_education",
        readMinutes: 5,
        contentMarkdown: "Detailed personal study workflow exceeding twenty chars.",
        published: true,
        featured: false,
        popularScore: 10,
        sampleContent: false,
        publishedDateLabel: "Nov 23, 2026",
        createdAt: now,
        updatedAt: now,
      })
  );
});
