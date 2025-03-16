package com.vtp.vipo.seller.common.dao.entity.base;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;

import java.io.Serializable;
import java.util.Properties;

/**
 * This class implements the IdentifierGenerator interface to provide a custom ID generation
 * strategy using the Snowflake algorithm. The class overrides the configure and generate methods of
 * the IdentifierGenerator interface. The class also defines an enum AttributeType to represent the
 * type of the ID attribute.
 *
 * @author haidv
 * @version 1.0
 */
public class SnowflakeGeneratorStrategy implements IdentifierGenerator {

    /**
     * The type of the ID attribute.
     */
    private AttributeType idType;

    /**
     * This method is used to configure the ID generator. It sets the idType field based on the
     * returned class of the type parameter.
     *
     * @param type            the type of the ID attribute
     * @param params          the parameters of the ID generator
     * @param serviceRegistry the service registry
     * @throws MappingException if the returned class of the type parameter is not supported
     */
    @Override
    public void configure(Type type, Properties params, ServiceRegistry serviceRegistry)
            throws MappingException {
        idType = AttributeType.valueOf(type.getReturnedClass());
    }

    /**
     * This method is used to generate an ID for an entity. If the entity is a BaseEntity and its ID
     * is not null, it returns the ID. Otherwise, it generates a new ID using the Snowflake algorithm
     * and returns it.
     *
     * @param session the session
     * @param object  the entity
     * @return the ID of the entity
     * @throws HibernateException if the entity is not a BaseEntity
     */
    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object)
            throws HibernateException {
        if (object instanceof BaseEntity obj && obj.getId() != null) {
            return idType.cast(obj.getId());
        }
        return idType.cast(Snowflake.getInstance().nextId());
    }

    /**
     * This enum represents the type of the ID attribute. It provides a method to cast a Long ID to
     * the appropriate type.
     */
    public enum AttributeType {
        /**
         * The LONG type represents a Long ID attribute.
         */
        LONG {
            /**
             * This method is used to cast a Long ID to a Long.
             *
             * @param id the Long ID
             * @return the Long ID
             */
            @Override
            public Serializable cast(Long id) {
                return id;
            }
        };

        /**
         * This method is used to get the AttributeType value of a class. It returns LONG if the class
         * is assignable from Long. Otherwise, it throws a HibernateException.
         *
         * @param clazz the class
         * @return the AttributeType value of the class
         * @throws HibernateException if the class is not assignable from Long
         */
        static AttributeType valueOf(Class<?> clazz) {
            if (Long.class.isAssignableFrom(clazz)) {
                return LONG;
            } else {
                throw new HibernateException(
                        String.format(
                                "The @Tsid annotation on [%s] can only be placed on a Long or String entity attribute!",
                                clazz));
            }
        }

        /**
         * This abstract method should be implemented by the enum constants to cast a Long ID to the
         * appropriate type.
         *
         * @param id the Long ID
         * @return the ID cast to the appropriate type
         */
        public abstract Serializable cast(Long id);
    }
}
