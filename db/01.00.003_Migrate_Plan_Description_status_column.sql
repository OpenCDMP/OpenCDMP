DO $$DECLARE
   this_version CONSTANT varchar := '01.00.003';
BEGIN
   PERFORM * FROM "DBVersion" WHERE version = this_version;
   IF FOUND THEN RETURN; END IF;

   ALTER TABLE public."Plan"
      ADD COLUMN status_id uuid;
   ALTER TABLE public."Plan"
      ADD CONSTRAINT "Plan_status_fkey" FOREIGN KEY (status_id)
      REFERENCES public."PlanStatus" (id)
      ON UPDATE NO ACTION
      ON DELETE NO ACTION
      NOT VALID;

   UPDATE public."Plan" SET
   status_id = 'cb3ced76-9807-4829-82da-75777de1bc78'
   WHERE status = 0;

   UPDATE public."Plan" SET
   status_id = 'f1a3da63-0bff-438f-8b46-1a81ca176115'
   WHERE status = 1;

   ALTER TABLE IF EXISTS public."Plan" DROP COLUMN IF EXISTS status;
   ALTER TABLE public."Plan" ALTER COLUMN status_id SET NOT NULL;
   ALTER TABLE IF EXISTS public."Plan" RENAME status_id TO status;


   ALTER TABLE public."Description"
      ADD COLUMN status_id uuid;
   ALTER TABLE public."Description"
      ADD CONSTRAINT "Description_status_fkey" FOREIGN KEY (status_id)
      REFERENCES public."DescriptionStatus" (id)
      ON UPDATE NO ACTION
      ON DELETE NO ACTION
      NOT VALID;

   UPDATE public."Description" SET
   status_id = '978e6ff6-b5e9-4cee-86cb-bc7401ec4059'
   WHERE status = 0;

   UPDATE public."Description" SET
   status_id = 'c266e2ee-9ae9-4a2f-9b4b-bc6fb1dd54aa'
   WHERE status = 1;

   UPDATE public."Description" SET
   status_id = '60f5e529-7ed3-4be1-8754-ac8c7443f246'
   WHERE status = 2;

   ALTER TABLE IF EXISTS public."Description" DROP COLUMN IF EXISTS status;
   ALTER TABLE public."Description" ALTER COLUMN status_id SET NOT NULL;
   ALTER TABLE IF EXISTS public."Description" RENAME status_id TO status;
	
   
   INSERT INTO public."DBVersion" VALUES ('DMPDB', '01.00.003', '2024-09-19 12:00:00.000000+02', now(), 'Migrate status collumns.');

END$$;