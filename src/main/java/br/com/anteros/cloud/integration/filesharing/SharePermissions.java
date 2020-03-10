package br.com.anteros.cloud.integration.filesharing;


public class SharePermissions {

    public enum SingleRight {
        READ(1),
        UPDATE(2),
        CREATE(4),
        DELETE(8),
        SHARE(16);

        private final int intValue;

        private SingleRight(int iV) {
            intValue= iV;
        }

        public int getIntValue() {
            return intValue;
        }
    }

    private final int currentPermission;

    public SharePermissions(int currentPermission) {
        this.currentPermission = currentPermission;
    }

    public SharePermissions(SingleRight... permissions) {
        int calculatedPermission = 0;
        for(SingleRight permission: permissions)
        {
            calculatedPermission += permission.getIntValue();
        }
        this.currentPermission = calculatedPermission;
    }

    public boolean hasAllRights()
    {
        return currentPermission == (
                SingleRight.READ.getIntValue()+
                SingleRight.UPDATE.getIntValue()+
                SingleRight.CREATE.getIntValue()+
                SingleRight.DELETE.getIntValue()+
                SingleRight.SHARE.getIntValue()
                );
    }

    public boolean hasRight(SingleRight permission) {
        return (currentPermission & permission.intValue) != 0;
    }

    public int getCurrentPermission() {
        return currentPermission;
    }

    @Override
    public String toString() {
        return Integer.toString(currentPermission);
    }
}
