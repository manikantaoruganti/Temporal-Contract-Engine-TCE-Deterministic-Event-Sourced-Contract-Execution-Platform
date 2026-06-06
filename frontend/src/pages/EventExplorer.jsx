import { useState } from 'react';
import { Box, Typography, Autocomplete, TextField, Chip, Paper, Grid } from '@mui/material';
import { motion } from 'framer-motion';
import Editor from '@monaco-editor/react';
import dayjs from 'dayjs';
import PageHeader from '../components/PageHeader';
import { useContracts, useContractEvents } from '../api/hooks';
import { COLORS } from '../theme/theme';

const EVENT_COLORS = {
  CONTRACT_CREATED: COLORS.success,
  RULE_ADDED: COLORS.info,
  CONTRACT_EXECUTING: COLORS.secondary,
  CONTRACT_EXECUTED: COLORS.success,
  EXECUTION_FAILED: COLORS.error,
  CONTRACT_CANCELLED: COLORS.text.muted,
  CONTRACT_DEAD_LETTERED: COLORS.error,
  ExecutionLogged: COLORS.warning,
};

export default function EventExplorer() {
  const { data: contracts } = useContracts();
  const [selectedId, setSelectedId] = useState(null);
  const { data: events, isLoading } = useContractEvents(selectedId);
  const [selectedEvent, setSelectedEvent] = useState(null);
  const [filter, setFilter] = useState('');

  const contractOptions = contracts?.map(c => ({ label: `${c.name} (${c.id.split('-')[0]}...)`, id: c.id })) || [];

  const filteredEvents = events?.filter(e => e.eventType.includes(filter) || e.payload.includes(filter)) || [];

  return (
    <Box sx={{ height: 'calc(100vh - 120px)', display: 'flex', flexDirection: 'column' }}>
      <PageHeader title="Event Explorer" subtitle="Immutable audit trail and state reconstruction timeline" />

      <Box sx={{ display: 'flex', gap: 2, mb: 3 }}>
        <Autocomplete
          options={contractOptions}
          sx={{ width: 300 }}
          renderInput={(params) => <TextField {...params} label="Select Contract" />}
          onChange={(_, val) => setSelectedId(val?.id || null)}
        />
        <TextField
          label="Filter Events"
          value={filter}
          onChange={(e) => setFilter(e.target.value)}
          sx={{ flexGrow: 1 }}
          disabled={!selectedId}
        />
      </Box>

      {!selectedId ? (
        <Box sx={{ flexGrow: 1, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <Typography color="text.secondary">Select a contract to explore its event timeline.</Typography>
        </Box>
      ) : (
        <Grid container spacing={3} sx={{ flexGrow: 1, overflow: 'hidden' }}>
          <Grid item xs={12} md={5} sx={{ height: '100%', overflow: 'auto', pr: 1 }}>
            <Box sx={{ position: 'relative', pl: 2 }}>
              <Box sx={{ position: 'absolute', left: 24, top: 0, bottom: 0, width: 2, bgcolor: COLORS.border }} />
              {filteredEvents.map((evt, idx) => {
                const color = EVENT_COLORS[evt.eventType] || COLORS.primary;
                const isSelected = selectedEvent?.eventId === evt.eventId;
                return (
                  <motion.div
                    key={evt.eventId}
                    initial={{ opacity: 0, x: -20 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ delay: idx * 0.05 }}
                  >
                    <Box
                      onClick={() => setSelectedEvent(evt)}
                      sx={{
                        position: 'relative',
                        mb: 3,
                        ml: 3,
                        p: 2,
                        cursor: 'pointer',
                        bgcolor: isSelected ? 'rgba(99,102,241,0.1)' : COLORS.bg.card,
                        border: `1px solid ${isSelected ? color : COLORS.border}`,
                        borderRadius: 2,
                        transition: 'all 0.2s',
                        '&::before': {
                          content: '""',
                          position: 'absolute',
                          left: -32,
                          top: 16,
                          width: 12,
                          height: 12,
                          borderRadius: '50%',
                          bgcolor: color,
                          boxShadow: `0 0 10px ${color}`,
                        }
                      }}
                    >
                      <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block', mb: 0.5 }}>
                        {dayjs(evt.occurredAt).format('YYYY-MM-DD HH:mm:ss.SSS')}
                      </Typography>
                      <Chip label={evt.eventType} size="small" sx={{ bgcolor: `${color}22`, color, fontWeight: 'bold' }} />
                    </Box>
                  </motion.div>
                );
              })}
            </Box>
          </Grid>
          
          <Grid item xs={12} md={7} sx={{ height: '100%' }}>
            <Paper sx={{ height: '100%', bgcolor: '#1e1e1e', overflow: 'hidden', display: 'flex', flexDirection: 'column' }}>
              <Box sx={{ p: 1.5, borderBottom: `1px solid ${COLORS.border}`, bgcolor: '#252526' }}>
                <Typography variant="body2" sx={{ fontFamily: 'monospace', color: '#ccc' }}>
                  {selectedEvent ? `Event Payload: ${selectedEvent.eventId}` : 'Select an event to inspect payload'}
                </Typography>
              </Box>
              <Box sx={{ flexGrow: 1 }}>
                {selectedEvent && (
                  <Editor
                    height="100%"
                    defaultLanguage="json"
                    theme="vs-dark"
                    value={JSON.stringify(JSON.parse(selectedEvent.payload), null, 2)}
                    options={{ readOnly: true, minimap: { enabled: false }, fontSize: 13 }}
                  />
                )}
              </Box>
            </Paper>
          </Grid>
        </Grid>
      )}
    </Box>
  );
}
