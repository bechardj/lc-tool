package us.jbec.lct.upgrade;

import org.springframework.transaction.annotation.Transactional;

public interface Upgrade {
    public boolean optional();
    @Transactional
    public void execute() throws RuntimeException;
}
