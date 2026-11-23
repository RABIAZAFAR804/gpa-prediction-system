package com.example.data.model

object SampleBlogs {
    val articles: List<BlogArticle> = listOf(
        BlogArticle(
            id = "sample_ai_gpa_forecasting",
            authorId = "editorial_team",
            authorName = "Dr. Elena Vance",
            authorRole = "Director of Learning Analytics",
            title = "How Predictive AI Models Forecast Semester GPA Before Finals",
            subtitle = "Discover which leading indicators—attendance consistency, midterm velocity, and study cadence—carry the strongest signal.",
            category = "AI in Education",
            tags = listOf("AI", "Predictive Analytics", "Midterms", "Strategy"),
            coverKey = "img_blog_ai_education",
            readMinutes = 7L,
            published = true,
            featured = true,
            popularScore = 980L,
            sampleContent = true,
            publishedDateLabel = "Nov 18, 2026",
            contentMarkdown = """
                ## Why Leading Academic Signals Beat End-of-Term Surprises
                Most students only discover their true GPA trajectory after final exam grades post—when the opportunity to course-correct has already closed. Modern predictive academic analytics invert that timeline by evaluating leading behavioral and assessment signals at Weeks 6 through 10.

                ## The Four Highest-Weight Predictors
                Across multi-semester university cohorts, four variables account for over 78% of variance in final semester GPA:
                - Attendance Consistency: Dropping below 85% attendance creates compounding conceptual gaps in cumulative STEM and humanities courses.
                - Formative Assignment Velocity: Early submission quality and steady homework averages stabilize grade floors before high-stakes exams.
                - Midterm Calibration: Comparing your midterm percentile against course weighting reveals exact point thresholds needed on finals.
                - Cognitive Recovery & Sleep: Chronic sleep under 6.5 hours degrades recall retention by up to 28% during exam weeks.

                ## Turning Predictions Into Weekly Action
                A forecast is only valuable when paired with a counterfactual plan. Use the Predictor tab to test how adding three focused hours per week or boosting assignment mastery shifts your projected GPA band and lowers volatility risk.

                ## Practical Checklist for Week 8 Onward
                Audit each syllabus by remaining grade weight. Allocate 65% of your weekly study budget to courses where a single letter-grade jump yields the highest credit-weighted GPA lift.
            """.trimIndent()
        ),
        BlogArticle(
            id = "sample_gpa_recovery_blueprint",
            authorId = "editorial_team",
            authorName = "Prof. Marcus Thorne",
            authorRole = "Academic Strategy Fellow",
            title = "The Weighted Credit Blueprint: Lifting a 3.2 GPA Above 3.7",
            subtitle = "Why credit-weighted prioritization outperforms uniform studying when every point matters.",
            category = "GPA Improvement",
            tags = listOf("GPA", "Credit Weighting", "Prioritization"),
            coverKey = "img_blog_gpa_strategy",
            readMinutes = 6L,
            published = true,
            featured = true,
            popularScore = 915L,
            sampleContent = true,
            publishedDateLabel = "Nov 15, 2026",
            contentMarkdown = """
                ## Stop Treating Every Course as Equal Weight
                A 4-credit Algorithms or Organic Chemistry course shifts your semester GPA twice as much as a 2-credit seminar. High-performing scholars budget weekly cognitive energy strictly in proportion to credit weight and current grade proximity to the next cutoff.

                ## Identifying High-Leverage Grade Cutoffs
                Look closely at courses sitting between 87% and 89.5%. Moving a 4-credit course from a B+ (3.3) to an A- (3.7) adds +1.60 quality points—often outweighing pushing a 95% course to 98%.

                ## Executing a 14-Day Grade Stabilization Sprint
                - Map every remaining quiz, lab, and final weight in a single ledger.
                - Book office hours within 48 hours of any graded assignment below 82%.
                - Replace passive rereading with timed past-paper drills calibrated to exam rubrics.
            """.trimIndent()
        ),
        BlogArticle(
            id = "sample_active_recall_protocols",
            authorId = "editorial_team",
            authorName = "Dr. Priya Nair",
            authorRole = "Cognitive Learning Researcher",
            title = "Active Recall & Interleaved Practice: Retaining Twice as Much in Less Time",
            subtitle = "Evidence-based study protocols that replace marathon highlighting with durable exam retrieval.",
            category = "Study Techniques",
            tags = listOf("Active Recall", "Spaced Repetition", "Memory"),
            coverKey = "img_blog_ai_education",
            readMinutes = 8L,
            published = true,
            featured = false,
            popularScore = 890L,
            sampleContent = true,
            publishedDateLabel = "Nov 12, 2026",
            contentMarkdown = """
                ## The Fluency Illusion of Rereading
                Highlighting textbooks feels productive because visual familiarity rises, yet recall under closed-book exam pressure stays flat. Cognitive science consistently shows that retrieval effort—not exposure duration—builds neural pathways.

                ## The 3-Pass Retrieval Cycle
                - Pass 1 (Blank Sheet Brain Dump): Immediately after lecture, write everything you remember without notes for 7 minutes, then fill gaps in a second ink color.
                - Pass 2 (Spaced Question Deck): Convert lecture headings into "Why" and "How" prompts reviewed at 24h, 72h, and 7-day intervals.
                - Pass 3 (Interleaved Problem Sets): Mix problem types from three chapters instead of blocking one chapter at a time.

                ## Measuring Mastery Before Exam Day
                If you can explain a core concept aloud in 90 seconds without glancing at notes and solve two novel problems cold, that module is exam-ready.
            """.trimIndent()
        ),
        BlogArticle(
            id = "sample_time_blocking_deep_work",
            authorId = "editorial_team",
            authorName = "Liam Chen, M.Ed.",
            authorRole = "Productivity Architect",
            title = "90-Minute Ultradian Study Blocks for Heavy STEM & Thesis Loads",
            subtitle = "Align your hardest analytical problem sets with peak circadian focus windows.",
            category = "Time Management",
            tags = listOf("Time Blocking", "Deep Work", "Focus"),
            coverKey = "img_blog_gpa_strategy",
            readMinutes = 5L,
            published = true,
            featured = false,
            popularScore = 840L,
            sampleContent = true,
            publishedDateLabel = "Nov 09, 2026",
            contentMarkdown = """
                ## Why Fragmented 20-Minute Study Sessions Fail
                Attention residue lingers for up to 23 minutes after checking notifications. Studying in short, interrupted bursts traps students in shallow review loops while extending total desk hours late into the night.

                ## Designing Your Daily Anchor Blocks
                - Morning Anchor (90 min): Hardest quantitative problem sets or thesis synthesis before checking email or social feeds.
                - Afternoon Maintenance (60 min): Lab prep, code debugging, and readings between classes.
                - Evening Wrap (30 min): Spaced repetition flashcards and tomorrow's 3-item priority list.

                ## Protecting Recovery Buffers
                Schedule a hard stop each evening. Knowing your study day ends at 8:30 PM dramatically increases daytime urgency and focus quality.
            """.trimIndent()
        ),
        BlogArticle(
            id = "sample_sleep_burnout_wellness",
            authorId = "editorial_team",
            authorName = "Dr. Hannah Brooks",
            authorRole = "University Wellness Psychologist",
            title = "Preventing Midterm Burnout: Sleep Architecture & Cognitive Resilience",
            subtitle = "How 7.5 hours of sleep and stress recovery cycles protect your GPA during peak exam seasons.",
            category = "Mental Wellness for Students",
            tags = listOf("Wellness", "Sleep", "Burnout Prevention"),
            coverKey = "img_blog_wellness",
            readMinutes = 6L,
            published = true,
            featured = false,
            popularScore = 875L,
            sampleContent = true,
            publishedDateLabel = "Nov 06, 2026",
            contentMarkdown = """
                ## The Hidden GPA Cost of All-Nighters
                Memory consolidation—specifically transferring problem-solving patterns from the hippocampus to neocortical storage—occurs predominantly during slow-wave and REM sleep. Sacrificing sleep before an exam destroys the exact recall machinery you studied to build.

                ## Three Science-Backed Stress Reset Habits
                - Consistent Wake Anchor: Keep your wake time within 45 minutes even on weekends to stabilize circadian alertness.
                - 10-Minute Daylight Walks: Natural morning light plus movement lowers cortisol after intense lab or exam sprints.
                - Worry Offload Journal: Spend 5 minutes writing unresolved tasks before bed so your working memory disengages.

                ## Sustainable Excellence Over Short Sprints
                The highest-achieving graduating seniors rarely work the longest hours—they maintain steady energy across all 15 weeks of the term.
            """.trimIndent()
        ),
        BlogArticle(
            id = "sample_final_exam_war_room",
            authorId = "editorial_team",
            authorName = "Prof. David Alverez",
            authorRole = "Assessment & Curriculum Chair",
            title = "The 10-Day Final Exam Playbook: Triage, Mock Simulations & Error Logs",
            subtitle = "A step-by-step countdown system to peak on cumulative university finals without panic.",
            category = "Exam Preparation",
            tags = listOf("Finals", "Mock Exams", "Error Log"),
            coverKey = "img_blog_gpa_strategy",
            readMinutes = 7L,
            published = true,
            featured = false,
            popularScore = 810L,
            sampleContent = true,
            publishedDateLabel = "Nov 03, 2026",
            contentMarkdown = """
                ## Days 10–7: Diagnostic Triage
                Never start final prep by reading Week 1 slides in order. Take a timed diagnostic practice exam on Day 10 to expose which topics are solid versus fragile.

                ## Days 6–3: The Mistake Ledger Method
                Create a dedicated error log for every missed practice question. Categorize each miss as Concept Gap, Formula Recall, or Time Pressure, and drill only those weak nodes.

                ## Days 2–1: Closed-Book Calibration & Rest
                Complete one final timed simulation 48 hours before exam day. Spend the final evening doing light summary review and securing 8 hours of sleep.
            """.trimIndent()
        ),
        BlogArticle(
            id = "sample_academic_success_habits",
            authorId = "editorial_team",
            authorName = "Sophia Patel",
            authorRole = "Dean's List Peer Mentor",
            title = "From First-Year Transition to Dean's List: Mastering Office Hours & Syllabi",
            subtitle = "Unspoken academic habits that separate consistent 3.8+ scholars from overwhelmed peers.",
            category = "Academic Success",
            tags = listOf("Dean's List", "Office Hours", "Habits"),
            coverKey = "img_blog_wellness",
            readMinutes = 5L,
            published = true,
            featured = false,
            popularScore = 760L,
            sampleContent = true,
            publishedDateLabel = "Oct 29, 2026",
            contentMarkdown = """
                ## Treating the Syllabus as a Strategic Contract
                In Week 1, extract every exam date, project milestone, and grading rubric into one master calendar. Knowing where peak workload clusters occur in Week 7 and Week 12 eliminates last-minute collisions.

                ## How to Use Office Hours Effectively
                Bring one specific attempted problem or thesis outline rather than asking "I don't understand Chapter 4." Faculty remember prepared students who engage deeply with material early in the term.

                ## Building Accountability Micro-Groups
                A two- or three-person study pod that meets twice weekly for 60 minutes of quiet problem-solving multiplies persistence through challenging core classes.
            """.trimIndent()
        ),
        BlogArticle(
            id = "sample_university_life_balance",
            authorId = "editorial_team",
            authorName = "Noah Lindqvist",
            authorRole = "Student Leadership Advisor",
            title = "Thriving in University Life: Balancing Clubs, Research & Coursework",
            subtitle = "How to say yes to meaningful campus leadership without eroding your academic foundation.",
            category = "University Life",
            tags = listOf("Campus Life", "Research", "Balance"),
            coverKey = "img_blog_wellness",
            readMinutes = 5L,
            published = true,
            featured = false,
            popularScore = 720L,
            sampleContent = true,
            publishedDateLabel = "Oct 24, 2026",
            contentMarkdown = """
                ## Depth Over Resume Collecting
                Joining six student clubs dilutes both your leadership impact and your study bandwidth. Choose two high-alignment commitments: one technical or research lab, and one community or creative outlet.

                ## Communicating Early During Heavy Weeks
                When research symposiums or hackathons overlap with coursework, notify lab teammates and instructors five days ahead rather than disappearing under stress.

                ## Designing Your Campus Environment
                Separate your relaxation space from your deep-study space. Reserve a quiet library floor exclusively for focused coursework so your brain triggers concentration on arrival.
            """.trimIndent()
        ),
        BlogArticle(
            id = "sample_career_gpa_portfolios",
            authorId = "editorial_team",
            authorName = "Claire Sterling",
            authorRole = "STEM Career Strategist",
            title = "Beyond the Transcript: Pairing Strong GPA With Internships & Research Portfolios",
            subtitle = "How recruiters and graduate admissions committees evaluate GPA alongside real-world execution.",
            category = "Career Development",
            tags = listOf("Career", "Internships", "Grad School"),
            coverKey = "img_blog_ai_education",
            readMinutes = 6L,
            published = true,
            featured = false,
            popularScore = 795L,
            sampleContent = true,
            publishedDateLabel = "Oct 19, 2026",
            contentMarkdown = """
                ## How GPA Opens Doors—and What Carries You Through
                Competitive fellowships, honors programs, and top engineering internships frequently use a 3.5+ GPA threshold as an initial filter. Maintaining strong academic fundamentals keeps every career lane open.

                ## Translating Coursework Into Portfolio Artifacts
                - Turn standout class capstones into documented case studies with clear metrics and architecture diagrams.
                - Ask professors whose courses you aced for research assistantships during sophomore and junior terms.
                - Connect analytical habits—hypothesis testing, structured writing, quantitative rigor—directly to internship interviews.

                ## Planning Your Multi-Semester Trajectory
                Use GPA analytics each term to protect your academic floor while carving out dedicated Friday blocks for career outreach and technical portfolio polishing.
            """.trimIndent()
        )
    )
}
