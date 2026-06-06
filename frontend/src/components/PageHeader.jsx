import { Typography, Box } from '@mui/material';
import { motion } from 'framer-motion';

export default function PageHeader({ title, subtitle, action, icon }) {
  return (
    <motion.div initial={{ opacity: 0, y: -10 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.4 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 4 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          {icon && <Box sx={{ color: 'primary.main', display: 'flex', alignItems: 'center' }}>{icon}</Box>}
          <Box>
            <Typography variant="h4" sx={{ fontWeight: 700 }}>{title}</Typography>
            {subtitle && <Typography variant="body2" sx={{ mt: 0.5, color: 'text.secondary' }}>{subtitle}</Typography>}
          </Box>
        </Box>
        {action && <Box>{action}</Box>}
      </Box>
    </motion.div>
  );
}
