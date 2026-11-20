package ar.edu.utn.frc.dsi.bolsines.modelo;

import jakarta.persistence.*;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Entity
public class Usuario {
    private static final SecureRandom RANDOM = new SecureRandom();

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String nombre;
    /** Hash PBKDF2 "salt:hash" en Base64, nunca la contraseña en claro. */
    @Column(nullable = false)
    private String contrasena;

    protected Usuario() {}

    public Usuario(String nombre, String contrasenaPlana) {
        this.nombre = nombre;
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        this.contrasena = b64(salt) + ":" + b64(pbkdf2(contrasenaPlana, salt));
    }

    public String getNombre() { return nombre; }

    public Long getId() { return id; }

    public boolean validarContrasena(String contrasenaPlana) {
        String[] partes = contrasena.split(":");
        byte[] salt = Base64.getDecoder().decode(partes[0]);
        return MessageDigest.isEqual(Base64.getDecoder().decode(partes[1]), pbkdf2(contrasenaPlana, salt));
    }

    private static byte[] pbkdf2(String contrasenaPlana, byte[] salt) {
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(new PBEKeySpec(contrasenaPlana.toCharArray(), salt, 210_000, 256))
                    .getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String b64(byte[] bytes) { return Base64.getEncoder().encodeToString(bytes); }
}
