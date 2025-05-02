package org.codenova.groupware.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class AddNote {
    private String content;
    private List<String> receiverIds;
}
