CREATE TABLE IF NOT EXISTS public."MaintenanceRun" (
    id uuid NOT NULL,
    preset character varying(100),
    status smallint NOT NULL,
    stop_on_failure boolean NOT NULL,
    stop_requested boolean NOT NULL,
    created_by uuid,
    created_at timestamp with time zone NOT NULL,
    finished_at timestamp with time zone,
    heartbeat_at timestamp with time zone NOT NULL,
    CONSTRAINT "MaintenanceRun_pkey" PRIMARY KEY (id)
);

-- At most one run in progress (status 0), across all replicas.
CREATE UNIQUE INDEX IF NOT EXISTS "MaintenanceRun_one_running_idx" ON public."MaintenanceRun" ((true)) WHERE status = 0;

CREATE TABLE IF NOT EXISTS public."MaintenanceTaskRun" (
    id uuid NOT NULL,
    run uuid NOT NULL,
    task character varying(100) NOT NULL,
    ordinal integer NOT NULL,
    status smallint NOT NULL,
    started_at timestamp with time zone,
    finished_at timestamp with time zone,
    error text,
    CONSTRAINT "MaintenanceTaskRun_pkey" PRIMARY KEY (id),
    CONSTRAINT "MaintenanceTaskRun_run_fkey" FOREIGN KEY (run) REFERENCES public."MaintenanceRun" (id)
);

CREATE INDEX IF NOT EXISTS "MaintenanceTaskRun_run_idx" ON public."MaintenanceTaskRun" (run);
CREATE INDEX IF NOT EXISTS "MaintenanceTaskRun_task_finished_idx" ON public."MaintenanceTaskRun" (task, finished_at);
