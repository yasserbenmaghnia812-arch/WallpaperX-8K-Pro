/*
# Lock Pinterest cache to the server

1. Purpose
   The Pinterest cache is an internal server store. Android clients call the
   edge function and must not read, insert, edit, or delete cache rows directly.

2. Security changes
   - Revoke all table privileges from anon and authenticated.
   - Keep row-level security enabled as defense in depth.
   - The edge function continues to access the table using its server-only role.

3. Important note
   No wallpaper data is deleted or changed. Search behavior remains available
   through the deployed Pinterest search function.
*/

REVOKE ALL PRIVILEGES ON TABLE public.pinterest_search_cache FROM anon;
REVOKE ALL PRIVILEGES ON TABLE public.pinterest_search_cache FROM authenticated;
