package com.passkeyper.android.vaultmodel;

import android.os.Parcel;

/**
 * Represents a security question and an answer for an entry in the vault.
 */
public class SecurityQuesEntry extends VaultModel {

    /**
     * The Parcelable Creator for SecurityQuesEntry.
     */
    public static final Creator<SecurityQuesEntry> CREATOR = new Creator<SecurityQuesEntry>() {

        protected SecurityQuesEntry newFromParcel(Parcel parcel) {
            return new SecurityQuesEntry(parcel);
        }

        public SecurityQuesEntry[] newArray(int size) {
            return new SecurityQuesEntry[size];
        }
    };

    /* Data structure that holds all 'answer' strings for security reasons */
    private static final SecureStringData strings = new SecureStringData();

    /* The question */
    private String question;
    /* The record this security question data corresponds to */
    private final EntryRecord record;

    /**
     * Creates a new, empty SecurityQuesEntry associated with the given EntryRecord.
     *
     * @param record the EntryRecord that this SecurityQuesEntry is associated with.
     */
    public SecurityQuesEntry(EntryRecord record) {
        this.record = record;
    }

    /* Create from Parcel */
    private SecurityQuesEntry(Parcel in) {
        record = in.readParcelable(EntryRecord.class.getClassLoader());
        question = in.readString();
    }

    public EntryRecord getRecord() {
        return record;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public char[] getAnswer() {
        if (strings.contains(key))
            return strings.get(key);
        else
            throw new IllegalStateException("SecurityQuesEntry answer has been accessed after it has been erased from memory");
    }

    public void setAnswer(char[] answer) {
        strings.put(answer, key);
    }

    @Override
    protected void saveToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(record, i);
        parcel.writeString(question);
    }

    /**
     * Erases all of the char[]'s in memory that contain sensitive data.
     */
    public static void eraseSecureMemory() {
        strings.eraseAll();
    }

}
