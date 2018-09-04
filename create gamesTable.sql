CREATE TABLE games (
	id int not null auto_increment,
    win int not null default 0,
    loss int not null default 0,
    draw int not null default 0,
    CONSTRAINT pk PRIMARY KEY(id)
);