package br.com.anteros.cloud.integration.filesharing;

import java.security.InvalidParameterException;


public enum ItemType {
	FOLDER("folder"),
	FILE("file");

    private final String itemTypeStr;

    private ItemType(String itemTypeStr) {
        this.itemTypeStr = itemTypeStr;
    }

    public String getItemTypeStr() {
        return itemTypeStr;
    }
    
    public static ItemType getItemByName(String name)
    {
        for (ItemType t : ItemType.values())
        {
            if (t.getItemTypeStr().equals(name))
            {
                return t;
            }
        }
        throw new InvalidParameterException("Encontrado tipo inválido <"+name+">");
    }
    
    
}
