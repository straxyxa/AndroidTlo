package com.example.androidtlo;

import android.os.Parcel;
import android.os.Parcelable;

public class PostepInfo implements Parcelable {
    public long mPobranychBajtow; // Количество загруженных байтов
    public long mRozmiar;        // Общий размер файла
    public String mStatus;       // Статус загрузки: "Загрузка", "Завершено", "Ошибка"

    public PostepInfo(long pobranychBajtow, long rozmiar, String status) {
        this.mPobranychBajtow = pobranychBajtow;
        this.mRozmiar = rozmiar;
        this.mStatus = status;
    }

    protected PostepInfo(Parcel in) {
        mPobranychBajtow = in.readLong();
        mRozmiar = in.readLong();
        mStatus = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mPobranychBajtow);
        dest.writeLong(mRozmiar);
        dest.writeString(mStatus);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PostepInfo> CREATOR = new Creator<PostepInfo>() {
        @Override
        public PostepInfo createFromParcel(Parcel in) {
            return new PostepInfo(in);
        }

        @Override
        public PostepInfo[] newArray(int size) {
            return new PostepInfo[size];
        }
    };
}
