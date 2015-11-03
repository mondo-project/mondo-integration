//
// Autogenerated by Thrift Compiler (0.9.3)
//
// DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
//


//HELPER FUNCTIONS AND STRUCTURES

Users_createUser_args = function(args) {
  this.username = null;
  this.password = null;
  this.profile = null;
  if (args) {
    if (args.username !== undefined && args.username !== null) {
      this.username = args.username;
    } else {
      throw new Thrift.TProtocolException(Thrift.TProtocolExceptionType.UNKNOWN, 'Required field username is unset!');
    }
    if (args.password !== undefined && args.password !== null) {
      this.password = args.password;
    } else {
      throw new Thrift.TProtocolException(Thrift.TProtocolExceptionType.UNKNOWN, 'Required field password is unset!');
    }
    if (args.profile !== undefined && args.profile !== null) {
      this.profile = new UserProfile(args.profile);
    } else {
      throw new Thrift.TProtocolException(Thrift.TProtocolExceptionType.UNKNOWN, 'Required field profile is unset!');
    }
  }
};
Users_createUser_args.prototype = {};
Users_createUser_args.prototype.read = function(input) {
  input.readStructBegin();
  while (true)
  {
    var ret = input.readFieldBegin();
    var fname = ret.fname;
    var ftype = ret.ftype;
    var fid = ret.fid;
    if (ftype == Thrift.Type.STOP) {
      break;
    }
    switch (fid)
    {
      case 1:
      if (ftype == Thrift.Type.STRING) {
        this.username = input.readString().value;
      } else {
        input.skip(ftype);
      }
      break;
      case 2:
      if (ftype == Thrift.Type.STRING) {
        this.password = input.readString().value;
      } else {
        input.skip(ftype);
      }
      break;
      case 3:
      if (ftype == Thrift.Type.STRUCT) {
        this.profile = new UserProfile();
        this.profile.read(input);
      } else {
        input.skip(ftype);
      }
      break;
      default:
        input.skip(ftype);
    }
    input.readFieldEnd();
  }
  input.readStructEnd();
  return;
};

Users_createUser_args.prototype.write = function(output) {
  output.writeStructBegin('Users_createUser_args');
  if (this.username !== null && this.username !== undefined) {
    output.writeFieldBegin('username', Thrift.Type.STRING, 1);
    output.writeString(this.username);
    output.writeFieldEnd();
  }
  if (this.password !== null && this.password !== undefined) {
    output.writeFieldBegin('password', Thrift.Type.STRING, 2);
    output.writeString(this.password);
    output.writeFieldEnd();
  }
  if (this.profile !== null && this.profile !== undefined) {
    output.writeFieldBegin('profile', Thrift.Type.STRUCT, 3);
    this.profile.write(output);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

Users_createUser_result = function(args) {
  this.err1 = null;
  if (args instanceof UserExists) {
    this.err1 = args;
    return;
  }
  if (args) {
    if (args.err1 !== undefined && args.err1 !== null) {
      this.err1 = args.err1;
    }
  }
};
Users_createUser_result.prototype = {};
Users_createUser_result.prototype.read = function(input) {
  input.readStructBegin();
  while (true)
  {
    var ret = input.readFieldBegin();
    var fname = ret.fname;
    var ftype = ret.ftype;
    var fid = ret.fid;
    if (ftype == Thrift.Type.STOP) {
      break;
    }
    switch (fid)
    {
      case 1:
      if (ftype == Thrift.Type.STRUCT) {
        this.err1 = new UserExists();
        this.err1.read(input);
      } else {
        input.skip(ftype);
      }
      break;
      case 0:
        input.skip(ftype);
        break;
      default:
        input.skip(ftype);
    }
    input.readFieldEnd();
  }
  input.readStructEnd();
  return;
};

Users_createUser_result.prototype.write = function(output) {
  output.writeStructBegin('Users_createUser_result');
  if (this.err1 !== null && this.err1 !== undefined) {
    output.writeFieldBegin('err1', Thrift.Type.STRUCT, 1);
    this.err1.write(output);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

Users_testCredentials_args = function(args) {
  this.username = null;
  this.password = null;
  if (args) {
    if (args.username !== undefined && args.username !== null) {
      this.username = args.username;
    } else {
      throw new Thrift.TProtocolException(Thrift.TProtocolExceptionType.UNKNOWN, 'Required field username is unset!');
    }
    if (args.password !== undefined && args.password !== null) {
      this.password = args.password;
    } else {
      throw new Thrift.TProtocolException(Thrift.TProtocolExceptionType.UNKNOWN, 'Required field password is unset!');
    }
  }
};
Users_testCredentials_args.prototype = {};
Users_testCredentials_args.prototype.read = function(input) {
  input.readStructBegin();
  while (true)
  {
    var ret = input.readFieldBegin();
    var fname = ret.fname;
    var ftype = ret.ftype;
    var fid = ret.fid;
    if (ftype == Thrift.Type.STOP) {
      break;
    }
    switch (fid)
    {
      case 1:
      if (ftype == Thrift.Type.STRING) {
        this.username = input.readString().value;
      } else {
        input.skip(ftype);
      }
      break;
      case 2:
      if (ftype == Thrift.Type.STRING) {
        this.password = input.readString().value;
      } else {
        input.skip(ftype);
      }
      break;
      default:
        input.skip(ftype);
    }
    input.readFieldEnd();
  }
  input.readStructEnd();
  return;
};

Users_testCredentials_args.prototype.write = function(output) {
  output.writeStructBegin('Users_testCredentials_args');
  if (this.username !== null && this.username !== undefined) {
    output.writeFieldBegin('username', Thrift.Type.STRING, 1);
    output.writeString(this.username);
    output.writeFieldEnd();
  }
  if (this.password !== null && this.password !== undefined) {
    output.writeFieldBegin('password', Thrift.Type.STRING, 2);
    output.writeString(this.password);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

Users_testCredentials_result = function(args) {
  this.success = null;
  if (args) {
    if (args.success !== undefined && args.success !== null) {
      this.success = args.success;
    }
  }
};
Users_testCredentials_result.prototype = {};
Users_testCredentials_result.prototype.read = function(input) {
  input.readStructBegin();
  while (true)
  {
    var ret = input.readFieldBegin();
    var fname = ret.fname;
    var ftype = ret.ftype;
    var fid = ret.fid;
    if (ftype == Thrift.Type.STOP) {
      break;
    }
    switch (fid)
    {
      case 0:
      if (ftype == Thrift.Type.BOOL) {
        this.success = input.readBool().value;
      } else {
        input.skip(ftype);
      }
      break;
      case 0:
        input.skip(ftype);
        break;
      default:
        input.skip(ftype);
    }
    input.readFieldEnd();
  }
  input.readStructEnd();
  return;
};

Users_testCredentials_result.prototype.write = function(output) {
  output.writeStructBegin('Users_testCredentials_result');
  if (this.success !== null && this.success !== undefined) {
    output.writeFieldBegin('success', Thrift.Type.BOOL, 0);
    output.writeBool(this.success);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

Users_updateProfile_args = function(args) {
  this.username = null;
  this.profile = null;
  if (args) {
    if (args.username !== undefined && args.username !== null) {
      this.username = args.username;
    } else {
      throw new Thrift.TProtocolException(Thrift.TProtocolExceptionType.UNKNOWN, 'Required field username is unset!');
    }
    if (args.profile !== undefined && args.profile !== null) {
      this.profile = new UserProfile(args.profile);
    } else {
      throw new Thrift.TProtocolException(Thrift.TProtocolExceptionType.UNKNOWN, 'Required field profile is unset!');
    }
  }
};
Users_updateProfile_args.prototype = {};
Users_updateProfile_args.prototype.read = function(input) {
  input.readStructBegin();
  while (true)
  {
    var ret = input.readFieldBegin();
    var fname = ret.fname;
    var ftype = ret.ftype;
    var fid = ret.fid;
    if (ftype == Thrift.Type.STOP) {
      break;
    }
    switch (fid)
    {
      case 1:
      if (ftype == Thrift.Type.STRING) {
        this.username = input.readString().value;
      } else {
        input.skip(ftype);
      }
      break;
      case 2:
      if (ftype == Thrift.Type.STRUCT) {
        this.profile = new UserProfile();
        this.profile.read(input);
      } else {
        input.skip(ftype);
      }
      break;
      default:
        input.skip(ftype);
    }
    input.readFieldEnd();
  }
  input.readStructEnd();
  return;
};

Users_updateProfile_args.prototype.write = function(output) {
  output.writeStructBegin('Users_updateProfile_args');
  if (this.username !== null && this.username !== undefined) {
    output.writeFieldBegin('username', Thrift.Type.STRING, 1);
    output.writeString(this.username);
    output.writeFieldEnd();
  }
  if (this.profile !== null && this.profile !== undefined) {
    output.writeFieldBegin('profile', Thrift.Type.STRUCT, 2);
    this.profile.write(output);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

Users_updateProfile_result = function(args) {
  this.err1 = null;
  if (args instanceof UserNotFound) {
    this.err1 = args;
    return;
  }
  if (args) {
    if (args.err1 !== undefined && args.err1 !== null) {
      this.err1 = args.err1;
    }
  }
};
Users_updateProfile_result.prototype = {};
Users_updateProfile_result.prototype.read = function(input) {
  input.readStructBegin();
  while (true)
  {
    var ret = input.readFieldBegin();
    var fname = ret.fname;
    var ftype = ret.ftype;
    var fid = ret.fid;
    if (ftype == Thrift.Type.STOP) {
      break;
    }
    switch (fid)
    {
      case 1:
      if (ftype == Thrift.Type.STRUCT) {
        this.err1 = new UserNotFound();
        this.err1.read(input);
      } else {
        input.skip(ftype);
      }
      break;
      case 0:
        input.skip(ftype);
        break;
      default:
        input.skip(ftype);
    }
    input.readFieldEnd();
  }
  input.readStructEnd();
  return;
};

Users_updateProfile_result.prototype.write = function(output) {
  output.writeStructBegin('Users_updateProfile_result');
  if (this.err1 !== null && this.err1 !== undefined) {
    output.writeFieldBegin('err1', Thrift.Type.STRUCT, 1);
    this.err1.write(output);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

Users_updatePassword_args = function(args) {
  this.username = null;
  this.newPassword = null;
  if (args) {
    if (args.username !== undefined && args.username !== null) {
      this.username = args.username;
    } else {
      throw new Thrift.TProtocolException(Thrift.TProtocolExceptionType.UNKNOWN, 'Required field username is unset!');
    }
    if (args.newPassword !== undefined && args.newPassword !== null) {
      this.newPassword = args.newPassword;
    } else {
      throw new Thrift.TProtocolException(Thrift.TProtocolExceptionType.UNKNOWN, 'Required field newPassword is unset!');
    }
  }
};
Users_updatePassword_args.prototype = {};
Users_updatePassword_args.prototype.read = function(input) {
  input.readStructBegin();
  while (true)
  {
    var ret = input.readFieldBegin();
    var fname = ret.fname;
    var ftype = ret.ftype;
    var fid = ret.fid;
    if (ftype == Thrift.Type.STOP) {
      break;
    }
    switch (fid)
    {
      case 1:
      if (ftype == Thrift.Type.STRING) {
        this.username = input.readString().value;
      } else {
        input.skip(ftype);
      }
      break;
      case 2:
      if (ftype == Thrift.Type.STRING) {
        this.newPassword = input.readString().value;
      } else {
        input.skip(ftype);
      }
      break;
      default:
        input.skip(ftype);
    }
    input.readFieldEnd();
  }
  input.readStructEnd();
  return;
};

Users_updatePassword_args.prototype.write = function(output) {
  output.writeStructBegin('Users_updatePassword_args');
  if (this.username !== null && this.username !== undefined) {
    output.writeFieldBegin('username', Thrift.Type.STRING, 1);
    output.writeString(this.username);
    output.writeFieldEnd();
  }
  if (this.newPassword !== null && this.newPassword !== undefined) {
    output.writeFieldBegin('newPassword', Thrift.Type.STRING, 2);
    output.writeString(this.newPassword);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

Users_updatePassword_result = function(args) {
  this.err1 = null;
  if (args instanceof UserNotFound) {
    this.err1 = args;
    return;
  }
  if (args) {
    if (args.err1 !== undefined && args.err1 !== null) {
      this.err1 = args.err1;
    }
  }
};
Users_updatePassword_result.prototype = {};
Users_updatePassword_result.prototype.read = function(input) {
  input.readStructBegin();
  while (true)
  {
    var ret = input.readFieldBegin();
    var fname = ret.fname;
    var ftype = ret.ftype;
    var fid = ret.fid;
    if (ftype == Thrift.Type.STOP) {
      break;
    }
    switch (fid)
    {
      case 1:
      if (ftype == Thrift.Type.STRUCT) {
        this.err1 = new UserNotFound();
        this.err1.read(input);
      } else {
        input.skip(ftype);
      }
      break;
      case 0:
        input.skip(ftype);
        break;
      default:
        input.skip(ftype);
    }
    input.readFieldEnd();
  }
  input.readStructEnd();
  return;
};

Users_updatePassword_result.prototype.write = function(output) {
  output.writeStructBegin('Users_updatePassword_result');
  if (this.err1 !== null && this.err1 !== undefined) {
    output.writeFieldBegin('err1', Thrift.Type.STRUCT, 1);
    this.err1.write(output);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

Users_deleteUser_args = function(args) {
  this.username = null;
  if (args) {
    if (args.username !== undefined && args.username !== null) {
      this.username = args.username;
    } else {
      throw new Thrift.TProtocolException(Thrift.TProtocolExceptionType.UNKNOWN, 'Required field username is unset!');
    }
  }
};
Users_deleteUser_args.prototype = {};
Users_deleteUser_args.prototype.read = function(input) {
  input.readStructBegin();
  while (true)
  {
    var ret = input.readFieldBegin();
    var fname = ret.fname;
    var ftype = ret.ftype;
    var fid = ret.fid;
    if (ftype == Thrift.Type.STOP) {
      break;
    }
    switch (fid)
    {
      case 1:
      if (ftype == Thrift.Type.STRING) {
        this.username = input.readString().value;
      } else {
        input.skip(ftype);
      }
      break;
      case 0:
        input.skip(ftype);
        break;
      default:
        input.skip(ftype);
    }
    input.readFieldEnd();
  }
  input.readStructEnd();
  return;
};

Users_deleteUser_args.prototype.write = function(output) {
  output.writeStructBegin('Users_deleteUser_args');
  if (this.username !== null && this.username !== undefined) {
    output.writeFieldBegin('username', Thrift.Type.STRING, 1);
    output.writeString(this.username);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

Users_deleteUser_result = function(args) {
  this.err1 = null;
  if (args instanceof UserNotFound) {
    this.err1 = args;
    return;
  }
  if (args) {
    if (args.err1 !== undefined && args.err1 !== null) {
      this.err1 = args.err1;
    }
  }
};
Users_deleteUser_result.prototype = {};
Users_deleteUser_result.prototype.read = function(input) {
  input.readStructBegin();
  while (true)
  {
    var ret = input.readFieldBegin();
    var fname = ret.fname;
    var ftype = ret.ftype;
    var fid = ret.fid;
    if (ftype == Thrift.Type.STOP) {
      break;
    }
    switch (fid)
    {
      case 1:
      if (ftype == Thrift.Type.STRUCT) {
        this.err1 = new UserNotFound();
        this.err1.read(input);
      } else {
        input.skip(ftype);
      }
      break;
      case 0:
        input.skip(ftype);
        break;
      default:
        input.skip(ftype);
    }
    input.readFieldEnd();
  }
  input.readStructEnd();
  return;
};

Users_deleteUser_result.prototype.write = function(output) {
  output.writeStructBegin('Users_deleteUser_result');
  if (this.err1 !== null && this.err1 !== undefined) {
    output.writeFieldBegin('err1', Thrift.Type.STRUCT, 1);
    this.err1.write(output);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

UsersClient = function(input, output) {
    this.input = input;
    this.output = (!output) ? input : output;
    this.seqid = 0;
};
UsersClient.prototype = {};
UsersClient.prototype.createUser = function(username, password, profile, callback) {
  this.send_createUser(username, password, profile, callback); 
  if (!callback) {
  this.recv_createUser();
  }
};

UsersClient.prototype.send_createUser = function(username, password, profile, callback) {
  this.output.writeMessageBegin('createUser', Thrift.MessageType.CALL, this.seqid);
  var args = new Users_createUser_args();
  args.username = username;
  args.password = password;
  args.profile = profile;
  args.write(this.output);
  this.output.writeMessageEnd();
  if (callback) {
    var self = this;
    this.output.getTransport().flush(true, function() {
      var result = null;
      try {
        result = self.recv_createUser();
      } catch (e) {
        result = e;
      }
      callback(result);
    });
  } else {
    return this.output.getTransport().flush();
  }
};

UsersClient.prototype.recv_createUser = function() {
  var ret = this.input.readMessageBegin();
  var fname = ret.fname;
  var mtype = ret.mtype;
  var rseqid = ret.rseqid;
  if (mtype == Thrift.MessageType.EXCEPTION) {
    var x = new Thrift.TApplicationException();
    x.read(this.input);
    this.input.readMessageEnd();
    throw x;
  }
  var result = new Users_createUser_result();
  result.read(this.input);
  this.input.readMessageEnd();

  if (null !== result.err1) {
    throw result.err1;
  }
  return;
};
UsersClient.prototype.testCredentials = function(username, password, callback) {
  this.send_testCredentials(username, password, callback); 
  if (!callback) {
    return this.recv_testCredentials();
  }
};

UsersClient.prototype.send_testCredentials = function(username, password, callback) {
  this.output.writeMessageBegin('testCredentials', Thrift.MessageType.CALL, this.seqid);
  var args = new Users_testCredentials_args();
  args.username = username;
  args.password = password;
  args.write(this.output);
  this.output.writeMessageEnd();
  if (callback) {
    var self = this;
    this.output.getTransport().flush(true, function() {
      var result = null;
      try {
        result = self.recv_testCredentials();
      } catch (e) {
        result = e;
      }
      callback(result);
    });
  } else {
    return this.output.getTransport().flush();
  }
};

UsersClient.prototype.recv_testCredentials = function() {
  var ret = this.input.readMessageBegin();
  var fname = ret.fname;
  var mtype = ret.mtype;
  var rseqid = ret.rseqid;
  if (mtype == Thrift.MessageType.EXCEPTION) {
    var x = new Thrift.TApplicationException();
    x.read(this.input);
    this.input.readMessageEnd();
    throw x;
  }
  var result = new Users_testCredentials_result();
  result.read(this.input);
  this.input.readMessageEnd();

  if (null !== result.success) {
    return result.success;
  }
  throw 'testCredentials failed: unknown result';
};
UsersClient.prototype.updateProfile = function(username, profile, callback) {
  this.send_updateProfile(username, profile, callback); 
  if (!callback) {
  this.recv_updateProfile();
  }
};

UsersClient.prototype.send_updateProfile = function(username, profile, callback) {
  this.output.writeMessageBegin('updateProfile', Thrift.MessageType.CALL, this.seqid);
  var args = new Users_updateProfile_args();
  args.username = username;
  args.profile = profile;
  args.write(this.output);
  this.output.writeMessageEnd();
  if (callback) {
    var self = this;
    this.output.getTransport().flush(true, function() {
      var result = null;
      try {
        result = self.recv_updateProfile();
      } catch (e) {
        result = e;
      }
      callback(result);
    });
  } else {
    return this.output.getTransport().flush();
  }
};

UsersClient.prototype.recv_updateProfile = function() {
  var ret = this.input.readMessageBegin();
  var fname = ret.fname;
  var mtype = ret.mtype;
  var rseqid = ret.rseqid;
  if (mtype == Thrift.MessageType.EXCEPTION) {
    var x = new Thrift.TApplicationException();
    x.read(this.input);
    this.input.readMessageEnd();
    throw x;
  }
  var result = new Users_updateProfile_result();
  result.read(this.input);
  this.input.readMessageEnd();

  if (null !== result.err1) {
    throw result.err1;
  }
  return;
};
UsersClient.prototype.updatePassword = function(username, newPassword, callback) {
  this.send_updatePassword(username, newPassword, callback); 
  if (!callback) {
  this.recv_updatePassword();
  }
};

UsersClient.prototype.send_updatePassword = function(username, newPassword, callback) {
  this.output.writeMessageBegin('updatePassword', Thrift.MessageType.CALL, this.seqid);
  var args = new Users_updatePassword_args();
  args.username = username;
  args.newPassword = newPassword;
  args.write(this.output);
  this.output.writeMessageEnd();
  if (callback) {
    var self = this;
    this.output.getTransport().flush(true, function() {
      var result = null;
      try {
        result = self.recv_updatePassword();
      } catch (e) {
        result = e;
      }
      callback(result);
    });
  } else {
    return this.output.getTransport().flush();
  }
};

UsersClient.prototype.recv_updatePassword = function() {
  var ret = this.input.readMessageBegin();
  var fname = ret.fname;
  var mtype = ret.mtype;
  var rseqid = ret.rseqid;
  if (mtype == Thrift.MessageType.EXCEPTION) {
    var x = new Thrift.TApplicationException();
    x.read(this.input);
    this.input.readMessageEnd();
    throw x;
  }
  var result = new Users_updatePassword_result();
  result.read(this.input);
  this.input.readMessageEnd();

  if (null !== result.err1) {
    throw result.err1;
  }
  return;
};
UsersClient.prototype.deleteUser = function(username, callback) {
  this.send_deleteUser(username, callback); 
  if (!callback) {
  this.recv_deleteUser();
  }
};

UsersClient.prototype.send_deleteUser = function(username, callback) {
  this.output.writeMessageBegin('deleteUser', Thrift.MessageType.CALL, this.seqid);
  var args = new Users_deleteUser_args();
  args.username = username;
  args.write(this.output);
  this.output.writeMessageEnd();
  if (callback) {
    var self = this;
    this.output.getTransport().flush(true, function() {
      var result = null;
      try {
        result = self.recv_deleteUser();
      } catch (e) {
        result = e;
      }
      callback(result);
    });
  } else {
    return this.output.getTransport().flush();
  }
};

UsersClient.prototype.recv_deleteUser = function() {
  var ret = this.input.readMessageBegin();
  var fname = ret.fname;
  var mtype = ret.mtype;
  var rseqid = ret.rseqid;
  if (mtype == Thrift.MessageType.EXCEPTION) {
    var x = new Thrift.TApplicationException();
    x.read(this.input);
    this.input.readMessageEnd();
    throw x;
  }
  var result = new Users_deleteUser_result();
  result.read(this.input);
  this.input.readMessageEnd();

  if (null !== result.err1) {
    throw result.err1;
  }
  return;
};
