create table artista (
    id bigserial primary key,
    nome varchar(255) not null,
    tipo varchar(20) not null
);

create table album (
    id bigserial primary key,
    nome varchar(255) not null
);

create table artista_album (
    artista_id bigint not null,
    album_id bigint not null,
    primary key (artista_id, album_id),
    constraint fk_artista foreign key (artista_id) references artista(id),
    constraint fk_album foreign key (album_id) references album(id)
);
