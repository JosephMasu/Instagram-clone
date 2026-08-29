export interface UserProfile {
  id: string;
  authUserId: string;
  username: string;
  firstName: string | null;
  lastName: string | null;
  bio: string | null;
  profilePictureUrl: string | null;
  isPrivate: boolean;
  createdAt: string;
  updatedAt: string;
  followerCount: number;
  followingCount: number;
  isFollowing: boolean | null;
}

export interface CreateProfileRequest {
  username: string;
  firstName?: string;
  lastName?: string;
  bio?: string;
  profilePictureUrl?: string;
  isPrivate?: boolean;
}

export interface UpdateProfileRequest {
  username?: string;
  firstName?: string;
  lastName?: string;
  bio?: string;
  profilePictureUrl?: string;
  isPrivate?: boolean;
}
