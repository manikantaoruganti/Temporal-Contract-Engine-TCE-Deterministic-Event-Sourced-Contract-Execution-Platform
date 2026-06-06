import { Box, Typography, Grid, Chip } from '@mui/material';
import { Info as InfoIcon } from '@mui/icons-material';
import { motion } from 'framer-motion';
import PageHeader from '../components/PageHeader';
import GlowCard from '../components/GlowCard';
import { COLORS } from '../theme/theme';

export default function AboutProject() {
  const sections = [
    {
      title: 'Why Event Sourcing?',
      desc: 'Instead of storing just the current state, we store a cryptographically immutable append-only log of every state transition. This provides a perfect audit trail, allows for temporal queries ("what was the state last Tuesday?"), and enables perfect state reconstruction.'
    },
    {
      title: 'Why Replayability?',
      desc: 'Because state is derived from events, we can drop the read database entirely and rebuild it from scratch. The Replay Engine proves the mathematical correctness of our system by verifying that Replay(Events) === CurrentState.'
    },
    {
      title: 'Why State Machines?',
      desc: 'Contracts follow a strict lifecycle (PENDING → EXECUTING → COMPLETED/FAILED). Explicit state machines prevent invalid transitions and ensure deterministic behavior during execution.'
    },
    {
      title: 'Why Dead Letter Queues?',
      desc: 'Distributed systems fail. When an execution fails repeatedly and exhausts its retry budget, it is routed to a Dead Letter Queue. This isolates the poison pill, preventing head-of-line blocking while preserving the stack trace for engineering review.'
    },
    {
      title: 'Why Idempotency?',
      desc: 'Network retries can cause duplicate requests. The engine uses idempotency keys and optimistic locking to guarantee that an execution happens exactly once, ensuring financial correctness.'
    },
    {
      title: 'Why Not Simple CRUD?',
      desc: 'Traditional CRUD overwrites history. In financial infrastructure, an UPDATE without a historical record is a compliance violation. This architecture prioritizes auditability, reliability, and correctness over simplicity.'
    }
  ];

  const techStack = [
    'Java 21', 'Spring Boot 3', 'PostgreSQL', 'Kafka', 'Redis', 'React 19', 'Material UI', 'Docker', 'Prometheus'
  ];

  return (
    <Box>
      <PageHeader title="About This Project" subtitle="Engineering philosophy and architectural decisions" icon={<InfoIcon fontSize="large" />} />

      <Box sx={{ mb: 6, textAlign: 'center', maxWidth: 800, mx: 'auto' }}>
        <Typography variant="h5" color="primary.light" sx={{ mb: 2, fontWeight: 300 }}>
          The Temporal Contract Engine is a deterministic backend platform that manages delayed, rule-bound commitments.
        </Typography>
        <Typography variant="body1" color="text.secondary">
          It is designed to demonstrate Staff-level engineering practices: Event Sourcing, CQRS, Distributed Messaging, and Reliability Engineering.
        </Typography>
      </Box>

      <Typography variant="h5" sx={{ mb: 3, fontWeight: 700 }}>Architectural Decisions</Typography>
      <Grid container spacing={4} sx={{ mb: 6 }}>
        {sections.map((sec, idx) => (
          <Grid item xs={12} md={6} key={sec.title}>
            <motion.div initial={{ opacity: 0, y: 20 }} whileInView={{ opacity: 1, y: 0 }} viewport={{ once: true }} transition={{ delay: idx * 0.1 }}>
              <GlowCard title={sec.title} sx={{ height: '100%', borderLeft: `4px solid ${COLORS.primary}` }}>
                <Typography variant="body2" sx={{ mt: 2, lineHeight: 1.7, color: 'text.secondary' }}>
                  {sec.desc}
                </Typography>
              </GlowCard>
            </motion.div>
          </Grid>
        ))}
      </Grid>

      <Typography variant="h5" sx={{ mb: 3, fontWeight: 700 }}>Technology Stack</Typography>
      <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1.5, mb: 6 }}>
        {techStack.map(tech => (
          <Chip key={tech} label={tech} sx={{ bgcolor: 'rgba(99,102,241,0.1)', color: COLORS.primaryLight, fontWeight: 'bold', px: 1, py: 2.5, borderRadius: 2 }} />
        ))}
      </Box>
    </Box>
  );
}
