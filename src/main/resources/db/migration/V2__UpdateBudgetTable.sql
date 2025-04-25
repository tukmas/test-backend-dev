begin;

update budget
set type = 'Расход'
where type = 'Комиссия';

commit;