import { Card, CardContent, Typography, Box } from '@mui/material';
import { alpha } from '@mui/material/styles';
import { motion } from 'framer-motion';

export default function GlowCard({ title, value, subtitle, icon, color = '#6366F1', trend, onClick, sx, children }) {
  return (
    <motion.div whileHover={{ y: -4, scale: 1.02 }} transition={{ type: 'spring', stiffness: 400, damping: 25 }}>
      <Card
        onClick={onClick}
        sx={{
          cursor: onClick ? 'pointer' : 'default',
          position: 'relative',
          overflow: 'hidden',
          '&::before': {
            content: '""',
            position: 'absolute',
            top: 0,
            left: 0,
            right: 0,
            height: '2px',
            background: `linear-gradient(90deg, transparent, ${color}, transparent)`,
          },
          ...sx,
        }}
      >
        <CardContent sx={{ p: 3 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 2 }}>
            <Typography variant="subtitle2" sx={{ fontSize: '0.7rem', textTransform: 'uppercase', letterSpacing: '0.1em', color: 'text.secondary' }}>
              {title}
            </Typography>
            {icon && (
              <Box sx={{ color, opacity: 0.8, display: 'flex', alignItems: 'center', p: 0.5, borderRadius: 1, backgroundColor: alpha(color, 0.1) }}>
                {icon}
              </Box>
            )}
          </Box>
          {value !== undefined && (
            <Typography variant="h3" sx={{ fontWeight: 800, color, lineHeight: 1, mb: 0.5, fontFamily: '"JetBrains Mono", monospace' }}>
              {value}
            </Typography>
          )}
          {subtitle && (
            <Typography variant="caption" sx={{ color: 'text.secondary' }}>
              {subtitle}
            </Typography>
          )}
          {trend !== undefined && (
            <Typography variant="caption" sx={{ color: trend >= 0 ? '#10B981' : '#EF4444', fontWeight: 600, ml: 1 }}>
              {trend >= 0 ? '↑' : '↓'} {Math.abs(trend)}%
            </Typography>
          )}
          {children}
        </CardContent>
      </Card>
    </motion.div>
  );
}
