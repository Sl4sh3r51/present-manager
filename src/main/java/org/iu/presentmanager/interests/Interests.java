package org.iu.presentmanager.interests;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
public class Interests {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private UUID id;

    @Column(name = "name")
    private String name;
}
