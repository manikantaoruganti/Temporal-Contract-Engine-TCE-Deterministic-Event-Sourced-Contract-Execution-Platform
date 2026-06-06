import { Box, Grid, Typography, CircularProgress } from '@mui/material';
import { motion } from 'framer-motion';
import { PieChart, Pie, Cell, BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts';
import {
  Assignment as ContractIcon,
  PendingActions as PendingIcon,
  PlayCircleOutline as ExecutingIcon,
  CheckCircleOutline as CompletedIcon,
  ErrorOutline as FailedIcon,
  CancelOutlined as CancelledIcon,
  Warning as DeadLetterIcon,
} from '@mui/icons-material';
import PageHeader from '../components/PageHeader';
import GlowCard from '../components/GlowCard';
import { useContracts, useDeadLetters } from '../api/hooks';
import { COLORS } from '../theme/theme';

export default function Dashboard() {
  const { data: contracts, isLoading } = useContracts(15000);
  const { data: deadLetters } = useDeadLetters();

  if (isLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 10 }}>
        <CircularProgress />
      </Box>
    );
  }

  const list = contracts || [];
  const stats = {
    total: list.length,
    pending: list.filter(c => c.status === 'PENDING').length,
    executing: list.filter(c => c.status === 'EXECUTING').length,
    completed: list.filter(c => c.status === 'COMPLETED').length,
    failed: list.filter(c => c.status === 'FAILED').length,
    cancelled: list.filter(c => c.status === 'CANCELLED').length,
  };

  const dlCount = deadLetters?.length || 0;

  const pieData = [
    { name: 'Completed', value: stats.completed, color: COLORS.success },
    { name: 'Failed', value: stats.failed, color: COLORS.error },
  ];

  const barData = [
    { name: 'Pending', count: stats.pending, fill: COLORS.warning },
    { name: 'Executing', count: stats.executing, fill: COLORS.secondary },
    { name: 'Completed', count: stats.completed, fill: COLORS.success },
    { name: 'Failed', count: stats.failed, fill: COLORS.error },
    { name: 'Cancelled', count: stats.cancelled, fill: COLORS.text.muted },
  ];

  const successRate = stats.total > 0 ? Math.round((stats.completed / stats.total) * 100) : 0;

  return (
    <Box>
      <PageHeader title="Operational Dashboard" subtitle="Real-time contract engine metrics" />

      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} md={3}>
          <GlowCard title="Total Contracts" value={stats.total} icon={<ContractIcon />} color={COLORS.primary} />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <GlowCard title="Success Rate" value={`${successRate}%`} icon={<CompletedIcon />} color={COLORS.success} />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <GlowCard title="Failed Executions" value={stats.failed} icon={<FailedIcon />} color={COLORS.error} />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <GlowCard title="Dead Letters" value={dlCount} icon={<DeadLetterIcon />} color={COLORS.warning} />
        </Grid>
      </Grid>

      <Grid container spacing={3}>
        <Grid item xs={12} md={8}>
          <GlowCard title="Status Distribution" sx={{ height: 400 }}>
            <Box sx={{ height: 300, mt: 2 }}>
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={barData}>
                  <XAxis dataKey="name" stroke={COLORS.text.secondary} />
                  <YAxis stroke={COLORS.text.secondary} />
                  <Tooltip contentStyle={{ backgroundColor: COLORS.bg.elevated, border: 'none', borderRadius: 8 }} />
                  <Bar dataKey="count" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </Box>
          </GlowCard>
        </Grid>
        <Grid item xs={12} md={4}>
          <GlowCard title="Completion Ratio" sx={{ height: 400 }}>
            <Box sx={{ height: 300, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie data={pieData} cx="50%" cy="50%" innerRadius={60} outerRadius={100} paddingAngle={5} dataKey="value">
                    {pieData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Pie>
                  <Tooltip contentStyle={{ backgroundColor: COLORS.bg.elevated, border: 'none', borderRadius: 8 }} />
                </PieChart>
              </ResponsiveContainer>
            </Box>
          </GlowCard>
        </Grid>
      </Grid>
    </Box>
  );
}
