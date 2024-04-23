package com.general.call;


import java.io.Serializable;

public class MediaDataProvider implements Serializable {

    public String callId;

    public String phoneNumber;

    public String toMemberType;

    public String toMemberName;

    public String toMemberImage;

    public String iTripId;

    public CommunicationManager.MEDIA media;

    public String vBookingNo;
    public boolean isBid = false;

    public boolean isVideoCall;

    /// Twilio
    public String fromMemberId;
    public String fromMemberType;
    public String toMemberId;
    public String roomName;

    private MediaDataProvider(Builder builder) {
        this.callId = builder.callId;
        this.phoneNumber = builder.phoneNumber;
        this.toMemberType = builder.toMemberType;
        this.toMemberName = builder.toMemberName;
        this.toMemberImage = builder.toMemberImage;
        this.iTripId = builder.iTripId;
        this.media = builder.media;
        this.vBookingNo = builder.vBookingNo;
        this.isBid = builder.isBid;

        this.isVideoCall = builder.isVideoCall;

        this.fromMemberId = builder.fromMemberId;
        this.fromMemberType = builder.fromMemberType;
        this.toMemberId = builder.toMemberId;
        this.roomName = builder.roomName;
    }

    public static class Builder {

        String callId;

        String phoneNumber;

        String toMemberType;

        String toMemberName;

        String toMemberImage;

        String iTripId;

        String vBookingNo;
        Boolean isBid = false;

        boolean isVideoCall;

        public Builder setVideoCall(boolean videoCall) {
            isVideoCall = videoCall;
            return this;
        }

        public Builder setBid(Boolean bid) {
            this.isBid = bid;
            return this;
        }

        CommunicationManager.MEDIA media;

        public Builder setMedia(CommunicationManager.MEDIA media) {
            this.media = media;
            return this;
        }

        public Builder setCallId(String callId) {
            this.callId = callId;
            return this;
        }

        public Builder setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public Builder setToMemberType(String toMemberType) {
            this.toMemberType = toMemberType;
            return this;
        }

        public Builder setToMemberName(String toMemberName) {
            this.toMemberName = toMemberName;
            return this;
        }

        public String getToMemberImage() {
            return toMemberImage;
        }

        public Builder setToMemberImage(String toMemberImage) {
            this.toMemberImage = toMemberImage;
            return this;
        }

        public String getTripId() {
            return iTripId;
        }

        public Builder setTripId(String iTripId) {
            this.iTripId = iTripId;
            return this;
        }

        public Builder setBookingNo(String vBookingNo) {
            this.vBookingNo = vBookingNo;
            return this;
        }

        public MediaDataProvider build() {
            return new MediaDataProvider(this);
        }

        /// Twilio
        public String fromMemberId;
        public String fromMemberType;
        public String toMemberId;
        public String roomName;

        public Builder setFromMemberId(String fromMemberId) {
            this.fromMemberId = fromMemberId;
            return this;
        }

        public Builder setFromMemberType(String fromMemberType) {
            this.fromMemberType = fromMemberType;
            return this;
        }

        public Builder setToMemberId(String toMemberId) {
            this.toMemberId = toMemberId;
            return this;
        }

        public Builder setRoomName(String roomName) {
            this.roomName = roomName;
            return this;
        }
    }
}
