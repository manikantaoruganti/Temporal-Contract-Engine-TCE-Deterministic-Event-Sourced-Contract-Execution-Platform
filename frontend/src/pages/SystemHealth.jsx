import { Box, Grid, Typography, CircularProgress, Chip } from '@mui/material';
import { MonitorHeart, Storage, Memory, Language, CheckCircleOutline, CancelOutlined } from '@mui/icons-material';
import { motion } from 'framer-motion';
import PageHeader from '../components/PageHeader';
import GlowCard from '../components/GlowCard';
import { useHealth } from '../api/hooks';
import { COLORS } from '../theme/theme';

export default function SystemHealth() {
  const { data: health, isLoading } = useHealth();

  if (isLoading) {
    return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 10 }}><CircularProgress /></Box>;
  }

  const isUp = health?.status === 'UP';
  const components = health?.components || {};

  return (
    <Box>
      <PageHeader title="System Health" subtitle="Real-time infrastructure monitoring" icon={<MonitorHeart fontSize="large" />} />

      <Box sx={{ 
        display: 'flex', flexDirection: 'column', alignItems: 'center', mb: 6, p: 4, 
        bgcolor: isUp ? 'rgba(16,185,129,0.1)' : 'rgba(239,68,68,0.1)',
        border: `2px solid ${isUp ? COLORS.success : COLORS.error}`,
        borderRadius: 3, boxShadow: `0 0 40px ${isUp ? 'rgba(16,185,129,0.2)' : 'rgba(239,68,68,0.2)'}`
      }}>
        <motion.div animate={{ scale: [1, 1.1, 1] }} transition={{ repeat: Infinity, duration: 2 }}>
          {isUp ? <CheckCircleOutline sx={{ fontSize: 80, color: COLORS.success, mb: 2 }} /> : <CancelOutlined sx={{ fontSize: 80, color: COLORS.error, mb: 2 }} />}
        </motion.div>
        <Typography variant="h2" sx={{ fontWeight: 900, color: isUp ? COLORS.success : COLORS.error, letterSpacing: '0.1em' }}>
          {isUp ? 'SYSTEM UP' : 'SYSTEM DOWN'}
        </Typography>
      </Box>

      <Typography variant="h6" sx={{ mb: 3 }}>Infrastructure Components</Typography>
      <Grid container spacing={3}>
        {Object.entries(components).map(([name, comp]) => (
          <Grid item xs={12} sm={6} md={4} key={name}>
            <GlowCard title={name.toUpperCase()} color={comp.status === 'UP' ? COLORS.success : COLORS.error}>
              <Box sx={{ mt: 2, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <Chip label={comp.status} sx={{ bgcolor: comp.status === 'UP' ? 'rgba(16,185,129,0.2)' : 'rgba(239,68,68,0.2)', color: comp.status === 'UP' ? COLORS.success : COLORS.error, fontWeight: 'bold' }} />
                {name.includes('db') && <Storage sx={{ color: 'text.secondary' }} />}
                {name.includes('redis') && <Memory sx={{ color: 'text.secondary' }} />}
                {name.includes('kafka') && <Language sx={{ color: 'text.secondary' }} />}
              </Box>
              {comp.details && (
                <Box sx={{ mt: 2, pt: 2, borderTop: `1px solid ${COLORS.border}` }}>
                  {Object.entries(comp.details).map(([k, v]) => (
                    <Box key={k} sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
                      <Typography variant="caption" color="text.secondary">{k}</Typography>
                      <Typography variant="caption">{String(v)}</Typography>
                    </Box>
                  ))}
                </Box>
              )}
            </GlowCard>
          </Grid>
        ))}
      </Grid>
    </Box>
  );
}
