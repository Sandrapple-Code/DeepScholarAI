package com.example.data.local

import kotlinx.coroutines.flow.first

object DatabaseInitializer {

    suspend fun populateIfEmpty(db: AppDatabase) {
        // Populate default user if empty
        val defaultUser = db.userDao().getUserByUsername("scholar")
        if (defaultUser == null) {
            db.userDao().insertUser(
                User(
                    username = "scholar",
                    passwordHash = "password",
                    bio = "Cognitive Computing & Astro-physics researcher. Searching for macro patterns in complex systemic networks.",
                    interests = "Artificial Intelligence, Quantum Mechanics, Astrobiology, Philosophy"
                )
            )
        }

        val projectCount = db.researchProjectDao().getAllProjects().first().size
        if (projectCount > 0) return // Already populated

        // 1. Research projects
        val aiProjId = db.researchProjectDao().insertProject(
            ResearchProject(
                name = "Artificial Intelligence",
                description = "Deep research covering Large Language Models, Prompt Engineering, Neural Networks, Alignment, and AI Ethics.",
                category = "Computer Science"
            )
        ).toInt()

        val quantumProjId = db.researchProjectDao().insertProject(
            ResearchProject(
                name = "Quantum Computing",
                description = "Investigating quantum mechanics foundations, superposition, quantum gates, entanglement, and modern qubits.",
                category = "Physics"
            )
        ).toInt()

        db.researchProjectDao().insertProject(
            ResearchProject(
                name = "Machine Learning",
                description = "Supervised, unsupervised, reinforcement learning, and standard neural architectures.",
                category = "Computer Science"
            )
        )

        db.researchProjectDao().insertProject(
            ResearchProject(
                name = "Ancient Civilizations",
                description = "Comparative historical study of Mesopotamia, Indus Valley, Bronze Age Collapse, and early writing systems.",
                category = "History"
            )
        )

        // 2. Default Saved Reports
        db.savedReportDao().insertReport(
            SavedReport(
                projectId = aiProjId,
                topic = "Large Language Models & GPT Architectures",
                overview = "Large Language Models (LLMs) are auto-regressive transformer models trained on massive text corpora to perform next-token prediction, gaining zero-shot and few-shot context reasoning capabilities as they scale up.",
                detailedAnalysis = "The core of modern LLMs is the Transformer decoder-only architecture utilizing Multi-Head Self-Attention (MHA) or Grouped-Query Attention (GQA). Scalability operates under power-law scaling lines correlating compute (FLOPs), dataset tokens, and active parameter counts.",
                keyInsights = "- Attention mechanism enables dynamic token relevance weights over high-dimensional vectors.\n- Scaling laws dictate predictable validation loss decreases with parameter and data sizes.\n- Post-training RLHF/DPO aligns raw models with instruction adherence and safety thresholds.",
                importantConcepts = "1. Self-Attention: Scaled dot-product query-key relevance.\n2. RMSNorm & SwiGLU: Modern activation and layer normalization layers.\n3. FlashAttention: Cache-friendly hardware attention calculation.",
                furtherReading = "- 'Attention Is All You Need' (Vaswani et al.)\n- 'Training Compute-Optimal Large Language Models' (Hoffmann et al.)",
                conclusion = "Generative transformers continue to dominate current natural language interfaces, moving toward agentic execution, external tool utilization, and reasoning-tree planning models.",
                isBookmarked = true
            )
        )

        db.savedReportDao().insertReport(
            SavedReport(
                projectId = quantumProjId,
                topic = "Intro to Quantum Superposition",
                overview = "Quantum Superposition represents the physical foundation stating a physical system can exist in a combinations of separate states simultaneously until an environmental interaction forces wavefunction reduction.",
                detailedAnalysis = "Mathematically represented as |ψ⟩ = α|0⟩ + β|1⟩, where complex amplitudes α and β satisfy the normalization check: |α|² + |β|² = 1. Active quantum state vectors inhabit a complex Hilbert space, modeled visually utilizing a unit radius Bloch Sphere.",
                keyInsights = "- Superposition is not a statistical blend, but physical wave-like interference mechanics.\n- Standard measurement collapses the wavefunction, outputting either eigenvalue deterministically.\n- Enables parallel quantum routing search spaces yielding subquadratic algorithms.",
                importantConcepts = "1. Bloch Sphere: Linear visual map mapping 2-level state space.\n2. Quantum State Density: Probability tracking through density matrices.\n3. Decoherence: Superposition loss caused by external noise.",
                furtherReading = "- 'Quantum Computation and Quantum Information' (Nielsen & Chuang)\n- Feynman Lectures on Physics (Vol 3)",
                conclusion = "Superposition, when linked via quantum entanglement, yields the primary exponential computing speedup fueling the development of superconducting and ion-trap quantum computers."
            )
        )

        // 3. Notes
        db.noteDao().insertNote(
            Note(
                workspace = "Artificial Intelligence",
                folder = "LLMs",
                title = "GPT-4 & Transformer Scaling Laws",
                content = "- OpenAI's GPT-4 continues to be based on dense and sparse Mixture of Experts (MoE).\n- MoE routes input tokens selectively via gating routers to active expert networks, keeping active FLOPs low while increasing total capacity.\n- Current scaling laws show performance improves smoothly as compute budget increases.",
                tags = "ai,llm,transformers",
                isFavorite = true
            )
        )
        db.noteDao().insertNote(
            Note(
                workspace = "Artificial Intelligence",
                folder = "Prompt Engineering",
                title = "Chain-of-Thought Patterns",
                content = "Few-shot prompting can be greatly enhanced by requesting reasoning paths:\n1. Prompt: 'Let's think step by step.'\n2. Tree of Thoughts (ToT): Run distinct branches of thoughts and let a voter select path.\n3. Self-Consistency: Run the prompt multiple times and aggregate standard matching responses.",
                tags = "prompting,reasoning",
                isFavorite = false
            )
        )
        db.noteDao().insertNote(
            Note(
                workspace = "Artificial Intelligence",
                folder = "AI Ethics",
                title = "Model Alignment & Safety",
                content = "- Sycophancy: Models tend to agree with incorrect human biases to maximize feedback scores.\n- Red-Teaming: Systematically provoking models to identify safety failures, hate speech, or leak vulnerabilities.",
                tags = "ethics,safety",
                isFavorite = false
            )
        )
        db.noteDao().insertNote(
            Note(
                workspace = "Physics",
                folder = "Quantum Mechanics",
                title = "Double-Slit Wavefunction Collapse",
                content = "- Young's double-slit experiment demonstrates wave-particle duality.\n- Inserting a particle detector at slit entrances collapses the quantum wave interference, rendering classic particle distribution.\n- Highlighting the role of measurement in basic physics.",
                tags = "physics,quantum",
                isFavorite = true
            )
        )
        db.noteDao().insertNote(
            Note(
                workspace = "Physics",
                folder = "Relativity",
                title = "Relativity & Curved Spacetime",
                content = "General Relativity models mass as curving the local spacetime fabric, which is seen as gravity:\n- Field equations: G_μν + Λ g_μν = 8πG/c⁴ T_μν\n- Mass-energy dictates spacetime curvature; spacetime curvature dictates Mass travel directions.",
                tags = "physics,gravity,einstein",
                isFavorite = false
            )
        )

        // 4. Research Papers
        db.researchPaperDao().insertPaper(
            ResearchPaper(
                category = "AI Papers",
                title = "Attention Is All You Need",
                authors = "Ashish Vaswani, Noam Shazeer, Niki Parmar, Jakob Uszkoreit, Llion Jones, Aidan N. Gomez, Łukasz Kaiser, Illia Polosukhin",
                publishDate = "2017-06-12",
                personalNotes = "The foundational paper introducing the Transformer architecture, replacing RNNs/CNNs with self-attention mechanism.",
                isBookmarked = true
            )
        )
        db.researchPaperDao().insertPaper(
            ResearchPaper(
                category = "AI Papers",
                title = "An Image is Worth 16x16 Words: Transformers for Image Recognition at Scale",
                authors = "Alexey Dosovitskiy, Lucas Beyer, Alexander Kolesnikov, Dirk Weissenborn, Xiaohua Zhai, Thomas Unterthiner, Mostafa Dehghani, et al.",
                publishDate = "2020-10-22",
                personalNotes = "Introduced ViT (Vision Transformer), showing self-attention matches CNNs on computer vision tasks.",
                isBookmarked = false
            )
        )
        db.researchPaperDao().insertPaper(
            ResearchPaper(
                category = "Physics Papers",
                title = "On the Electrodynamics of Moving Bodies",
                authors = "Albert Einstein",
                publishDate = "1905-09-26",
                personalNotes = "Einstein's original work presenting Special Relativity, unified Maxwell's electrodynamics and kinematics.",
                isBookmarked = true
            )
        )
        db.researchPaperDao().insertPaper(
            ResearchPaper(
                category = "Physics Papers",
                title = "Can Quantum-Mechanical Description of Physical Reality Be Considered Complete?",
                authors = "Albert Einstein, Boris Podolsky, Nathan Rosen",
                publishDate = "1935-05-15",
                personalNotes = "The famous 'EPR Paradox' paper contesting Quantum completeness and introducing quantum entanglement.",
                isBookmarked = false
            )
        )

        // 5. Planner Tasks
        db.plannerTaskDao().insertTask(
            PlannerTask(
                taskName = "Study GPT-4 Transformer mechanisms",
                targetDate = System.currentTimeMillis() + 12 * 3600 * 1000, // 12 hours from now
                priority = "HIGH",
                isCompleted = false,
                type = "DAILY"
            )
        )
        db.plannerTaskDao().insertTask(
            PlannerTask(
                taskName = "Draft AI structural ethics post",
                targetDate = System.currentTimeMillis() + 48 * 3600 * 1000,
                priority = "MEDIUM",
                isCompleted = false,
                type = "WEEKLY"
            )
        )
        db.plannerTaskDao().insertTask(
            PlannerTask(
                taskName = "Analyze Ancient Civilizations writing charts",
                targetDate = System.currentTimeMillis() + 5 * 24 * 3600 * 1000,
                priority = "LOW",
                isCompleted = false,
                type = "DEADLINE"
            )
        )
        db.plannerTaskDao().insertTask(
            PlannerTask(
                taskName = "Review Einstein-Podolsky-Rosen paradox paper",
                targetDate = System.currentTimeMillis() + 4 * 3600 * 1000,
                priority = "HIGH",
                isCompleted = true,
                type = "DAILY"
            )
        )

        // 6. Default Chat Threads & Messages
        val threadId1 = "66f7fbb1-4c6e-4ccf-b673-5a7c9d92f588"
        db.chatThreadDao().insertThread(
            ChatThread(
                id = threadId1,
                title = "Deep Generative Models Study",
                isPinned = true
            )
        )
        db.chatMessageDao().insertMessage(
            ChatMessage(
                threadId = threadId1,
                sender = "USER",
                text = "Can you help me understand GANs versus Diffusion models?"
            )
        )
        db.chatMessageDao().insertMessage(
            ChatMessage(
                threadId = threadId1,
                sender = "AI",
                text = "Certainly! Generative Adversarial Networks (GANs) utilize a competitive minimax game between a Generator and a Discriminator. Diffusion models, by contrast, are based on thermodynamic noise reversal—slowly adding Gaussian noise to an image, and training a continuous neural network (typically a U-Net) to reverse this process step-by-step. Diffusion is mathematically more stable than GANs, though traditionally slower to sample."
            )
        )

        val threadId2 = "8ef7fbb2-4c62-4cc2-b672-5a7c9d92fc89"
        db.chatThreadDao().insertThread(
            ChatThread(
                id = threadId2,
                title = "Schrödinger's Cat Explanations",
                isPinned = false
            )
        )
        db.chatMessageDao().insertMessage(
            ChatMessage(
                threadId = threadId2,
                sender = "USER",
                text = "Why did Schrödinger propose the cat experiment?"
            )
        )
        db.chatMessageDao().insertMessage(
            ChatMessage(
                threadId = threadId2,
                sender = "AI",
                text = "He proposed it as a reductio ad absurdum to highlight what he saw as incomplete parts of the Copenhagen interpretation. Specifically, how quantum wave mechanics could curve scale macroscopic entities (like a cat) into a ridiculous superposition of being simultaneously alive and dead until actual physical observation occurs, pointing to the need for a formal theory of measurement."
            )
        )
    }
}
