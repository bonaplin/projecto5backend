package aor.paj.bean;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;

@Singleton
@Startup
public class StartupBean {
    @Inject
    UserBean userBean;
    @Inject
    CategoryBean categoryBean;

    @PostConstruct
    public void init() {
        // Chama o método para criar o ProductOwner se não existir
        userBean.createDefaultUsersIfNotExistent();
        categoryBean.createDefaultCategoryIfNotExistent();
    }
}