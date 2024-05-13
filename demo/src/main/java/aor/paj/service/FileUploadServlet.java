package aor.paj.service;
import aor.paj.dao.TokenDao;
import aor.paj.dao.UserDao;
import aor.paj.entity.UserEntity;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@WebServlet("/upload")
@MultipartConfig
public class FileUploadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    @Inject
    private TokenDao tokenDao;
    @Inject
    private UserDao userDao;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (request.getContentType() != null && request.getContentType().startsWith("multipart/form-data")) {
            try {
                System.out.println("Upload de arquivo recebido.");
                String token = request.getHeader("token");
                if (token == null) {
                    response.getWriter().write("Token não fornecido.");
                    return;
                }
                System.out.println("Token: " + token);
                UserEntity userEntity = tokenDao.findUserByTokenString(token);
                if (userEntity == null) {
                    response.getWriter().write("Token inválido.");
                    return;
                }
                System.out.println("Usuário: " + userEntity.getUsername());

                Part filePart = request.getPart("file"); // Retrieves <input type="file" name="file">

                System.out.println("Arquivo: " + filePart.getSubmittedFileName());

                InputStream fileContent = filePart.getInputStream();

                System.out.println("Tamanho do arquivo: " + filePart.getSize() + " bytes");

                byte[] imageData = convertInputStreamToByteArray(fileContent);

                System.out.println("Tamanho dos dados do arquivo: " + imageData.length + " bytes");

                userEntity.setProfileImageData(imageData);
                System.out.println("Tipo da imagem: " + filePart.getContentType());
                userEntity.setProfileImageType(filePart.getContentType());
                System.out.println("Salvando imagem no banco de dados...");
                userDao.merge(userEntity);

                response.getWriter().write("Upload realizado com sucesso!");
            } catch (Exception e) {
                response.getWriter().write("Erro ao realizar o upload: " + e.getMessage());
            }
        } else {
            response.getWriter().write("A solicitação não é um formulário de upload de arquivo multipart.");
        }
    }

    private byte[] convertInputStreamToByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        return outputStream.toByteArray();
    }
}