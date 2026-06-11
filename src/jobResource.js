// Converts a SQLite job row into the JobExecutionResource JSON shape Atlas consumes.
// The job table uses id as both instanceId and executionId (no separate sequence needed).

export function jobToResource (row) {
  let params = {}
  if (row.params) {
    try { params = JSON.parse(row.params) } catch { /* ignore */ }
  }

  return {
    executionId: row.id,
    status: row.status || 'STARTED',
    exitStatus: row.exit_status || null,
    startDate: row.start_time || null,
    endDate: row.end_time || null,
    jobInstance: {
      instanceId: row.id,
      name: row.type || row.name
    },
    jobParameters: params,
    ownerType: 'ALL_JOB'
  }
}
