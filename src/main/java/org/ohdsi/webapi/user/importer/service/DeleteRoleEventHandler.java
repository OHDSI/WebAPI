package org.ohdsi.webapi.user.importer.service;

import com.odysseusinc.logging.event.DeleteRoleEvent;
import org.ohdsi.webapi.user.importer.repository.RoleGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class DeleteRoleEventHandler implements ApplicationListener<DeleteRoleEvent> {
    @Autowired
    private RoleGroupRepository roleGroupRepository;

    @Override
    public void onApplicationEvent(final DeleteRoleEvent event) {
        roleGroupRepository.deleteByRoleId(event.getId());
    }
}
