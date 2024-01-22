# QLNV
Database
create database QLNV;
use QLNV;


CREATE TABLE admin(
	id int AUTO_INCREMENT primary key not null, 
	username nvarchar(20), 
	password nvarchar(20));
CREATE TABLE employer(
	id int AUTO_INCREMENT primary key not null, 
    fname nvarchar(30) not null, 
    lname nvarchar(20) not null,
    tel nvarchar(10),
    description nvarchar(200),
    date date);
CREATE TABLE worker(
	id int AUTO_INCREMENT primary key not null, 
    fname nvarchar(30) not null, 
    lname nvarchar(20) not null,
    tel nvarchar(10),
    description nvarchar(200),
    iban nvarchar(100), 
    date date);
CREATE TABLE price(
	id int auto_increment primary key not null,
    fulltime decimal(18, 3),
    halftime decimal(18, 3),
    overtime decimal(18, 3),
    date date
);
CREATE TABLE job(
	id int auto_increment primary key not null,
    title nvarchar(100),
    employer_id integer references employer(id),
    price_id integer references price(id),
    description nvarchar(200),
	date date
);
create table worktype(
	id int auto_increment primary key not null,
    title nvarchar(100),
    no int,
    date date
);
create table workgroup(
	id int auto_increment primary key not null,
    job_id int references job(id),
    worktype_id int references worktype(id),
    workcount int,
    description nvarchar(200),
    date date
);
create table work(
	id int auto_increment primary key not null,
    job_id int references job(id),
    worker_id int references worker(id),
    worktype_id int references worktype(id),
    workgroup_id int references workgroup(id),
    description nvarchar(200),
    date date
);
create table invoice(
	id int auto_increment primary key not null,
    job_id int references job(id),
    amount decimal(18, 3),	
    date date
);
create table paytype(
	id int auto_increment primary key not null,
    title nvarchar(100),
    date date
);
create table payment(
	id int auto_increment primary key not null,
    worker_id int references worker(id),
    job_id int references job(id),
    paytype_id int references paytype(id),
    amount decimal(18, 3),	
    date date
);

select * from work;
DELETE from job where id=1;
SELECT * FROM payment WHERE worker_id=1;
SELECT * FROM worker ORDER BY id DESC LIMIT 1;
insert into admin (username, password) values ('admin', 1);
insert into price (fulltime, halftime, overtime) values (3200.000, 1600.000, 1200);
insert into paytype (title) values ("Salary"), ("Wage"), ("Cash"), ("Transfer"), ("Allowance"), ("Bonus");
insert into worktype (title, no) values ("manual work", 1), ("intellectual work", 2);


# Tech require
Add driver: "com.mysql.cj.jdbc.Driver"
Add Connection String: "jdbc:mysql://localhost:3306/QLNV"
Your also have to download file java database file: https://dev.mysql.com/downloads/connector/j/
And add it to IntelliJ IDEA as following: https://www.youtube.com/watch?v=T5Hey0e2Y_g
