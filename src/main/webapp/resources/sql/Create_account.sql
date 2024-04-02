create user c##Going identified by c##Going1234567;

grant connect, resource, dba to Going;

commit;

select * from all_users;