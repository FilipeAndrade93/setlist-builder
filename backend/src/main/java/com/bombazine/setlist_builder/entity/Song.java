package com.bombazine.setlist_builder.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "songs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Song {
}
