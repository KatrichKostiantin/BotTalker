class Response {
    private String pattern, addInfo;

    Response(String pattern, String addInfo) {
        this.pattern = pattern;
        this.addInfo = addInfo;
    }

    Response(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public String toString() {
        if (addInfo != null)
            return pattern.replace("*", addInfo);
        return pattern;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj.getClass() != this.getClass()) {
            return false;
        }
        final Response other = (Response) obj;
        if (addInfo != null)
            return pattern.equals(other.pattern) && addInfo.equals(other.addInfo);
        return pattern.equals(other.pattern);
    }

    public String getPattern() {
        return pattern;
    }

    public String getAddInfo() {
        return addInfo;
    }
}
