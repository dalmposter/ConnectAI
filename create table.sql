CREATE TABLE boardStates (
	state varchar(42) not null,
    move int not null,
    piece VARCHAR(1) not null,
    wins int not null,
    losses int not null,
    draws int not null,
    constraint pk primary key (state,move,piece)
);