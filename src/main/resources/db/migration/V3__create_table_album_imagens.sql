create table album_imagens (
    id bigserial primary key,
    url varchar(500) not null,
    album_id bigint not null,

    constraint fk_album_imagens_album
        foreign key (album_id)
        references album(id)
        on delete cascade
);