package ru.ifmo.ctddev.FSSARecSys.db;


/**
 * All object that implement this interface should be DB objects(related to their tables)
 * replace it
 */
public abstract class ORM {
    /**
     * save this object to the DB
     */
    public void save() {

    }

    /**
     * removing this object to the DB
     */
    public void delete() {

    }
}
