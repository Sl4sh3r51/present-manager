package org.iu.presentmanager.occasions;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
public class Occasions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "name")
    private String name;


}
