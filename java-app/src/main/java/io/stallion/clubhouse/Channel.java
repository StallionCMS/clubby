package io.stallion.clubhouse;

import java.util.List;
import java.util.Map;

import static io.stallion.utils.Literals.*;

import io.stallion.dataAccess.ModelBase;
import io.stallion.services.Log;

import javax.persistence.Column;
import javax.persistence.Table;

@Table(name="sch_channels")
public class Channel extends ModelBase {
    private String name = "";
    private ChannelType channelType = ChannelType.PUBLIC;

    @Column
    public String getName() {
        return name;
    }

    public Channel setName(String name) {
        this.name = name;
        return this;
    }

    @Column
    public ChannelType getChannelType() {
        return channelType;
    }

    public Channel setChannelType(ChannelType channelType) {
        this.channelType = channelType;
        return this;
    }
}
