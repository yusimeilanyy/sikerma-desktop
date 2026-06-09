package com.sikerma.sikerma.model;

import java.sql.Timestamp;
import java.time.LocalDate;

public class RenewalHistory {
    private int id;
    private int documentId;
    private LocalDate oldEndDate;
    private LocalDate newEndDate;
    private String previousStatus;
    private String newStatus;
    private String notes;
    private Timestamp createdAt;
    private String renewedBy;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getDocumentId() { return documentId; }
    public void setDocumentId(int documentId) { this.documentId = documentId; }

    public LocalDate getOldEndDate() { return oldEndDate; }
    public void setOldEndDate(LocalDate oldEndDate) { this.oldEndDate = oldEndDate; }

    public LocalDate getNewEndDate() { return newEndDate; }
    public void setNewEndDate(LocalDate newEndDate) { this.newEndDate = newEndDate; }

    public String getPreviousStatus() { return previousStatus; }
    public void setPreviousStatus(String previousStatus) { this.previousStatus = previousStatus; }

    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getRenewedBy() { return renewedBy; }
    public void setRenewedBy(String renewedBy) { this.renewedBy = renewedBy; }
}