package com.ecom.ai.ecomassistant.service;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public abstract class CrudService<T, ID, R extends CrudRepository<T, ID>> {

    protected final R repository;

    protected CrudService(R repository) {
        this.repository = repository;
    }

    public T save(T entity) {
        return repository.save(entity);
    }

    public Iterable<T> saveAll(Iterable<T> entities) {
        return repository.saveAll(entities);
    }

    public Optional<T> findById(ID id) {
        return repository.findById(id);
    }

    public boolean existsById(ID id) {
        return repository.existsById(id);
    }

    public List<T> findAll() {
        return (List<T>) repository.findAll();
    }

    public long count() {
        return repository.count();
    }
}