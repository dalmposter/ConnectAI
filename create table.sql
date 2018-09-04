CREATE TABLE boardStates (
	column0 int not null,
    column1 int not null,
    column2 int not null,
    column3 int not null,
    column4 int not null,
    column5 int not null,
    column6 int not null,
    move int not null,
    wins int not null,
    losses int not null,
    draws int not null,
    constraint pk primary key (column0, column1, column2, column3, column4, column5, column6, move)
);