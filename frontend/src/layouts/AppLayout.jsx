import { useState } from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import {
  Box, Drawer, List, ListItemButton, ListItemIcon, ListItemText, Typography,
  Divider, IconButton, Tooltip, Chip,
} from '@mui/material';
import { alpha } from '@mui/material/styles';
import {
  Dashboard as DashboardIcon,
  Description as ContractsIcon,
  Rule as RulesIcon,
  Timeline as EventsIcon,
  Replay as ReplayIcon,
  ListAlt as LogsIcon,
  ErrorOutline as DeadLetterIcon,
  MonitorHeart as MetricsIcon,
  Favorite as HealthIcon,
  AccountTree as ArchIcon,
  Info as AboutIcon,
  ChevronLeft,
  ChevronRight,
  Hub as LogoIcon,
} from '@mui/icons-material';
import { motion, AnimatePresence } from 'framer-motion';
import { useHealth } from '../api/hooks';

const DRAWER_WIDTH = 260;
const DRAWER_COLLAPSED = 72;

const NAV_ITEMS = [
  { path: '/', label: 'Dashboard', icon: <DashboardIcon /> },
  { path: '/contracts', label: 'Contracts', icon: <ContractsIcon /> },
  { path: '/rules', label: 'Rules', icon: <RulesIcon /> },
  { divider: true },
  { path: '/events', label: 'Event Explorer', icon: <EventsIcon /> },
  { path: '/replay', label: 'Replay Engine', icon: <ReplayIcon />, highlight: true },
  { path: '/execution-logs', label: 'Execution Logs', icon: <LogsIcon /> },
  { path: '/dead-letters', label: 'Dead Letters', icon: <DeadLetterIcon /> },
  { divider: true },
  { path: '/metrics', label: 'Metrics Center', icon: <MetricsIcon /> },
  { path: '/health', label: 'System Health', icon: <HealthIcon /> },
  { divider: true },
  { path: '/architecture', label: 'Architecture', icon: <ArchIcon /> },
  { path: '/about', label: 'About Project', icon: <AboutIcon /> },
];

export default function AppLayout() {
  const [collapsed, setCollapsed] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const { data: health } = useHealth();

  const drawerWidth = collapsed ? DRAWER_COLLAPSED : DRAWER_WIDTH;
  const isUp = health?.status === 'UP';

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh' }}>
      <Drawer
        variant="permanent"
        sx={{
          width: drawerWidth,
          flexShrink: 0,
          transition: 'width 0.3s ease',
          '& .MuiDrawer-paper': {
            width: drawerWidth,
            transition: 'width 0.3s ease',
            overflowX: 'hidden',
            background: 'linear-gradient(180deg, #111827 0%, #0B0F1A 100%)',
          },
        }}
      >
        {/* Logo */}
        <Box sx={{ p: 2, display: 'flex', alignItems: 'center', gap: 1.5, minHeight: 64 }}>
          <LogoIcon sx={{ color: 'primary.main', fontSize: 28 }} />
          {!collapsed && (
            <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }}>
              <Typography variant="subtitle1" sx={{ fontWeight: 800, color: 'primary.main', fontSize: '0.85rem', lineHeight: 1.2 }}>
                TEMPORAL<br />
                <Typography component="span" sx={{ color: 'text.secondary', fontWeight: 400, fontSize: '0.7rem' }}>
                  Contract Engine
                </Typography>
              </Typography>
            </motion.div>
          )}
        </Box>

        <Divider sx={{ mx: 1 }} />

        {/* Health indicator */}
        {!collapsed && (
          <Box sx={{ px: 2, py: 1.5 }}>
            <Chip
              size="small"
              label={isUp ? 'System Operational' : 'System Down'}
              sx={{
                width: '100%',
                fontWeight: 600,
                fontSize: '0.65rem',
                color: isUp ? '#10B981' : '#EF4444',
                backgroundColor: isUp ? 'rgba(16,185,129,0.1)' : 'rgba(239,68,68,0.1)',
                border: `1px solid ${isUp ? 'rgba(16,185,129,0.3)' : 'rgba(239,68,68,0.3)'}`,
              }}
            />
          </Box>
        )}

        {/* Navigation */}
        <List sx={{ px: 1, flex: 1 }}>
          {NAV_ITEMS.map((item, i) => {
            if (item.divider) return <Divider key={`d${i}`} sx={{ my: 1, mx: 1 }} />;
            const isActive = location.pathname === item.path || (item.path !== '/' && location.pathname.startsWith(item.path));
            return (
              <Tooltip key={item.path} title={collapsed ? item.label : ''} placement="right">
                <ListItemButton
                  onClick={() => navigate(item.path)}
                  sx={{
                    borderRadius: 2,
                    mb: 0.5,
                    py: 1,
                    minHeight: 42,
                    justifyContent: collapsed ? 'center' : 'initial',
                    backgroundColor: isActive ? alpha('#6366F1', 0.12) : 'transparent',
                    borderLeft: isActive ? '3px solid #6366F1' : '3px solid transparent',
                    '&:hover': { backgroundColor: alpha('#6366F1', 0.08) },
                    ...(item.highlight && !isActive ? {
                      background: 'linear-gradient(90deg, rgba(99,102,241,0.05), rgba(6,182,212,0.05))',
                    } : {}),
                  }}
                >
                  <ListItemIcon
                    sx={{
                      minWidth: 0,
                      mr: collapsed ? 0 : 2,
                      justifyContent: 'center',
                      color: isActive ? '#6366F1' : '#64748B',
                      fontSize: 20,
                    }}
                  >
                    {item.icon}
                  </ListItemIcon>
                  {!collapsed && (
                    <ListItemText
                      primary={item.label}
                      primaryTypographyProps={{
                        fontSize: '0.8rem',
                        fontWeight: isActive ? 700 : 500,
                        color: isActive ? '#F1F5F9' : '#94A3B8',
                      }}
                    />
                  )}
                  {!collapsed && item.highlight && (
                    <Chip label="HERO" size="small" sx={{ height: 18, fontSize: '0.55rem', fontWeight: 800, color: '#06B6D4', backgroundColor: 'rgba(6,182,212,0.12)', border: '1px solid rgba(6,182,212,0.3)' }} />
                  )}
                </ListItemButton>
              </Tooltip>
            );
          })}
        </List>

        {/* Collapse toggle */}
        <Box sx={{ p: 1, display: 'flex', justifyContent: 'center' }}>
          <IconButton onClick={() => setCollapsed(!collapsed)} size="small" sx={{ color: 'text.secondary' }}>
            {collapsed ? <ChevronRight /> : <ChevronLeft />}
          </IconButton>
        </Box>
      </Drawer>

      {/* Main content */}
      <Box component="main" sx={{ flexGrow: 1, p: { xs: 2, md: 4 }, minHeight: '100vh', overflow: 'auto' }}>
        <AnimatePresence mode="wait">
          <motion.div
            key={location.pathname}
            initial={{ opacity: 0, y: 12 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -12 }}
            transition={{ duration: 0.25 }}
          >
            <Outlet />
          </motion.div>
        </AnimatePresence>
      </Box>
    </Box>
  );
}
