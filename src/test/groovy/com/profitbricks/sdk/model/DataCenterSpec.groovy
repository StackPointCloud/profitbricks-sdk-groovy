package com.profitbricks.sdk.model

import spock.lang.*

/**
 * Created by fudge on 18.04.18.
 */
@Subject(DataCenter)
@Title("unit tests DataCenter CRUD")
@Stepwise
class DataCenterSpec extends Specification {

    @Subject
    DataCenter dataCenter
    @Shared
    String datacenterID

    final 'datacenters can be created'() {
        given: 'a DataCenter POGO'
        dataCenter = new DataCenter(name: "Groovy SDK Test", location: 'us/las', description: 'Groovy SDK test datacenter')

        when: 'its create() method is called'
        final _dc = dataCenter.create()

        then: 'the return value of that is a DataCenter POGO'
        _dc instanceof DataCenter

        and: 'it has a valid uuid'
        _dc.id
        UUID.fromString(_dc.id as String)

        cleanup:
        datacenterID = _dc.id
    }

    final 'datacenters can be listed'() {
        given: 'an empty DataCenter POGO'
        dataCenter = new DataCenter()

        when: 'its all property is called'
        final ids = dataCenter.all

        then: 'the result is non-empty and contains valid UUIDs'
        ids
        ids.size() == ids.collect {UUID.fromString(it)}.size()
    }

    final 'datacenters can be read'() {
        when: 'a DataCenter is retrieved'
        DataCenter _dc = new DataCenter(id: datacenterID).read() as DataCenter

        then: 'the returned object is a properly populated datacenter'
        _dc instanceof DataCenter
        _dc.id
        _dc.name
        _dc.location
    }

    final 'datacenters can be updated'() {
        given: 'a valid datacenter POGO'
        dataCenter = new DataCenter(id: datacenterID).read() as DataCenter
        final change = 'foo'

        when: 'its properties are updated'
        dataCenter.name = change
        dataCenter.description = change
        dataCenter.update()

        and: 'it is read again'
        dataCenter = dataCenter.read() as DataCenter

        then: 'the changes should be reflected'
        dataCenter.name == change
        dataCenter.description == change
    }

    final 'datacenters can be deleted'() {
        given: 'a valid datacenter POGO'
        dataCenter = new DataCenter(id: datacenterID).read() as DataCenter

        when: 'it is deleted'
        dataCenter.delete()

        and: 'retrieved again'
        dataCenter = new DataCenter(id: datacenterID).read() as DataCenter

        then: 'that result is empty'
        !dataCenter
    }
}
