package aor.paj.dto;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class StaticsUsers {
    private int countUsers;
    private int confirmedUsers;
    private int unconfirmedUsers;

    private double avgCountTasksPerUser;
    //• Gráfico que mostre o número de utilizadores registados ao longo do tempo (e.g.
    //gráfico de linhas). Os utilizadores apagados podem ser excluídos desta contagem.


//    @XmlElement

}
