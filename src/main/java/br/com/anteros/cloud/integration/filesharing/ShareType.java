package br.com.anteros.cloud.integration.filesharing;

import java.security.InvalidParameterException;

public enum ShareType {

    USER(0),
    GROUP(1),
    PUBLIC_LINK(3),
    EMAIL(4),
    FEDERATED_CLOUD_SHARE(6);

    private final int intValue;
    
    private ShareType(int iV) {
        intValue= iV;
    }

    public int getIntValue() {
        return intValue;
    }

    public static ShareType getShareTypeForIntValue(int i)
    {
        for (ShareType s : ShareType.values())
        {
            if (s.getIntValue() == i)
            {
                return s;
            }
        }
        throw new InvalidParameterException("Tipo de compartilhamento inválido encontrado " + i);
    }
    
}
