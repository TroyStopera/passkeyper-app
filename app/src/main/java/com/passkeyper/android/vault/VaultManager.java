package com.passkeyper.android.vault;

import android.content.Context;

import com.passkeyper.android.vault.local.LocalVaultManager;
import com.passkeyper.android.vaultmodel.AbstractVaultModel;
import com.passkeyper.android.vaultmodel.EntryRecord;
import com.passkeyper.android.vaultmodel.SecurityQuesEntry;
import com.passkeyper.android.vaultmodel.SensitiveEntry;
import com.passkeyper.android.vaultmodel.VaultModel;

import java.util.List;

/**
 * Defines the methods that need to be implemented in order to manage a vault.
 */
public abstract class VaultManager extends AbstractVaultModel.Manager {

    /* The instance of vault manager that the app should use */
    private static VaultManager instance = null;

    /**
     * Saves a VaultModel to the vault.
     *
     * @param vaultModel the model to save.
     */
    public final void save(VaultModel vaultModel) {
        if (vaultModel instanceof EntryRecord) save((EntryRecord) vaultModel);
        else if (vaultModel instanceof SensitiveEntry) save((SensitiveEntry) vaultModel);
        else if (vaultModel instanceof SecurityQuesEntry) save((SecurityQuesEntry) vaultModel);
    }

    /**
     * Deletes a VaultModel from the vault.
     *
     * @param vaultModel the model to delete.
     */
    public final void delete(VaultModel vaultModel) {
        if (vaultModel instanceof EntryRecord) delete((EntryRecord) vaultModel);
        else if (vaultModel instanceof SensitiveEntry) delete((SensitiveEntry) vaultModel);
        else if (vaultModel instanceof SecurityQuesEntry) delete((SecurityQuesEntry) vaultModel);
    }

    /**
     * @return all EntryRecords from the vault.
     */
    public abstract List<EntryRecord> getAllEntryRecords();

    /**
     * @param record the EntryRecord that the requested SensitiveEntries should be associated with.
     * @return a List of SensitiveData.
     */
    public abstract List<SensitiveEntry> getSensitiveEntries(EntryRecord record);

    /**
     * @param record the EntryRecord that the requested SecurityQuesEntries should be associated with.
     * @return a List of SecurityQuesEntry.
     */
    public abstract List<SecurityQuesEntry> getSecurityQuestions(EntryRecord record);

    /**
     * Saves an EntryRecord to the vault.
     *
     * @param record the EntryRecord to save.
     */
    public abstract void save(EntryRecord record);

    /**
     * Saves a SensitiveEntry to the vault.
     *
     * @param sensitiveEntry the SensitiveEntry to save.
     */
    public abstract void save(SensitiveEntry sensitiveEntry);

    /**
     * Saves a SecurityQuesEntry to the vault.
     *
     * @param securityQuesEntry the SecurityQuesEntry to save.
     */
    public abstract void save(SecurityQuesEntry securityQuesEntry);

    /**
     * Delete an EntryRecord from the vault. Implementations should also delete all related database rows.
     *
     * @param record the EntryRecord to delete.
     */
    public abstract void delete(EntryRecord record);

    /**
     * Delete a SensitiveEntry from the vault.
     *
     * @param sensitiveEntry the SensitiveEntry to delete.
     */
    public abstract void delete(SensitiveEntry sensitiveEntry);

    /**
     * Delete a SecurityQuesEntry from the vault.
     *
     * @param securityQuesEntry the SecurityQuesEntry to delete.
     */
    public abstract void delete(SecurityQuesEntry securityQuesEntry);

    /**
     * Returns the proper instance of VaultManager.
     *
     * @param context a Context used to inflate a VaultManager.
     * @return the instance of VaultManager.
     */
    public static VaultManager get(Context context) {
        //TODO: check user preferences for vault manager
        if (instance == null) instance = new LocalVaultManager(context);
        return instance;
    }

}
