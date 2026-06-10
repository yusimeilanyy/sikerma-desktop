package com.sikerma.sikerma.model;

import java.time.LocalDate;

public class Document {
    private int id;
    private String nomorDokumen;
    private String nomorDokumenPemda;
    private String nomorDokumenMitra;
    private String pemilik;
    private String jenis;
    private String mitra;
    private String kategori;
    private String jenisDokumenDetail;
    private LocalDate tanggalMulai;
    private LocalDate tanggalBerakhir;
    private String status;
    private String pic;
    private String picBlsdm;
    private String kontakPic;
    private String keterangan;
    private String filePath;

    // Getter dan Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNomorDokumen() { return nomorDokumen; }
    public void setNomorDokumen(String nomorDokumen) { this.nomorDokumen = nomorDokumen; }

    public String getNomorDokumenPemda() { return nomorDokumenPemda; }
    public void setNomorDokumenPemda(String nomorDokumenPemda) { this.nomorDokumenPemda = nomorDokumenPemda; }

    public String getNomorDokumenMitra() { return nomorDokumenMitra; }
    public void setNomorDokumenMitra(String nomorDokumenMitra) { this.nomorDokumenMitra = nomorDokumenMitra; }

    public String getPemilik() { return pemilik; }
    public void setPemilik(String pemilik) { this.pemilik = pemilik; }

    public String getJenis() { return jenis; }
    public void setJenis(String jenis) { this.jenis = jenis; }

    public String getMitra() { return mitra; }
    public void setMitra(String mitra) { this.mitra = mitra; }

    public String getKategori() { return kategori; }
    public void setKategori(String kategori) { this.kategori = kategori; }

    public String getJenisDokumenDetail() { return jenisDokumenDetail; }
    public void setJenisDokumenDetail(String jenisDokumenDetail) { this.jenisDokumenDetail = jenisDokumenDetail; }

    public LocalDate getTanggalMulai() { return tanggalMulai; }
    public void setTanggalMulai(LocalDate tanggalMulai) { this.tanggalMulai = tanggalMulai; }

    public LocalDate getTanggalBerakhir() { return tanggalBerakhir; }
    public void setTanggalBerakhir(LocalDate tanggalBerakhir) { this.tanggalBerakhir = tanggalBerakhir; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPic() { return pic; }
    public void setPic(String pic) { this.pic = pic; }

    public String getPicBlsdm() { return picBlsdm; }
    public void setPicBlsdm(String picBlsdm) { this.picBlsdm = picBlsdm; }

    public String getKontakPic() { return kontakPic; }
    public void setKontakPic(String kontakPic) { this.kontakPic = kontakPic; }

    public String getKeterangan() { return keterangan; }
    public void setKeterangan(String keterangan) { this.keterangan = keterangan; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
}