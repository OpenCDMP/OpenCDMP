CREATE TABLE IF NOT EXISTS public."Installation" (
    id uuid NOT NULL,
    CONSTRAINT "Installation_pkey" PRIMARY KEY (id)
);

-- Generated here rather than at startup, so replicas booting together share one id.
INSERT INTO public."Installation" (id)
SELECT uuid_generate_v4()
WHERE NOT EXISTS (SELECT 1 FROM public."Installation");
