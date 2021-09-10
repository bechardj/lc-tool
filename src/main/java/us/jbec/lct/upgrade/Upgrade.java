package us.jbec.lct.upgrade;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Interface for Upgrade
 */
public interface Upgrade {
    /**
     * Is the upgrade optional if it fails
     * @return is the upgrade optional
     */
    public boolean optional();

    /**
     * Execute the upgrade (should be transactional)
     * @throws RuntimeException
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void execute() throws RuntimeException;
}
