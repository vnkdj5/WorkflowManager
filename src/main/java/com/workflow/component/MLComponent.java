package com.workflow.component;

import java.util.HashMap;

public interface MLComponent extends Component {

    HashMap<String,Object> predict(HashMap<String,Object> df);
}
