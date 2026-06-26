package com.familyvault.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "folders")
public class Folder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private boolean defaultFolder;
    @Column(nullable = false)
    private boolean hidden;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locker_id", nullable = false)
    private Locker locker;
    @Column(nullable = false)
    private Instant createdAt = Instant.now();
    @OneToMany(mappedBy = "folder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VaultFile> files = new ArrayList<>();

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean isDefaultFolder() { return defaultFolder; }
    public void setDefaultFolder(boolean defaultFolder) { this.defaultFolder = defaultFolder; }
    public boolean isHidden() { return hidden; }
    public void setHidden(boolean hidden) { this.hidden = hidden; }
    public Locker getLocker() { return locker; }
    public void setLocker(Locker locker) { this.locker = locker; }
    public Instant getCreatedAt() { return createdAt; }
    public List<VaultFile> getFiles() { return files; }
}
