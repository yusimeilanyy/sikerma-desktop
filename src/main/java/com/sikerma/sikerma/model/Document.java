package com.sikerma.sikerma.model;

import java.time.LocalDate;

public class Document {
    private int id;
    private String nomorDokumen;
    private String jenis;
    private String mitra;
    private String kategori;
    private LocalDate tanggalMulai;
    private LocalDate tanggalBerakhir;
    private String pic;
    private String filePath;
    private String status;
    private LocalDate createdAt;

    // Constructor
    public Document() {}

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNomorDokumen() { return nomorDokumen; }
    public void setNomorDokumen(String nomorDokumen) { this.nomorDokumen = nomorDokumen; }

    public String getJenis() { return jenis; }
    public void setJenis(String jenis) { this.jenis = jenis; }

    public String getMitra() { return mitra; }
    public void setMitra(String mitra) { this.mitra = mitra; }

    public String getKategori() { return kategori; }
    public void setKategori(String kategori) { this.kategori = kategori; }

    public LocalDate getTanggalMulai() { return tanggalMulai; }
    public void setTanggalMulai(LocalDate tanggalMulai) { this.tanggalMulai = tanggalMulai; }

    public LocalDate getTanggalBerakhir() { return tanggalBerakhir; }
    public void setTanggalBerakhir(LocalDate tanggalBerakhir) { this.tanggalBerakhir = tanggalBerakhir; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPic() { return pic; }
    public void setPic(String pic) { this.pic = pic; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }
}