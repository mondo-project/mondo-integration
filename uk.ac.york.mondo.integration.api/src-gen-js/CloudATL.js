//
// Autogenerated by Thrift Compiler (0.9.3)
//
// DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
//


//HELPER FUNCTIONS AND STRUCTURES

CloudATL_launch_args = function(args) {
  this.transformation = null;
  this.source = null;
  this.target = null;
  if (args) {
    if (args.transformation !== undefined && args.transformation !== null) {
      this.transformation = args.transformation;
    } else {
      throw new Thrift.TProtocolException(Thrift.TProtocolExceptionType.UNKNOWN, 'Required field transformation is unset!');
    }
    if (args.source !== undefined && args.source !== null) {
      this.source = new ModelSpec(args.source);
    } else {
      throw new Thrift.TProtocolException(Thrift.TProtocolExceptionType.UNKNOWN, 'Required field source is unset!');
    }
    if (args.target !== undefined && args.target !== null) {
      this.target = new ModelSpec(args.target);
    } else {
      throw new Thrift.TProtocolException(Thrift.TProtocolExceptionType.UNKNOWN, 'Required field target is unset!');
    }
  }
};
CloudATL_launch_args.prototype = {};
CloudATL_launch_args.prototype.read = function(input) {
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
        this.transformation = input.readString().value;
      } else {
        input.skip(ftype);
      }
      break;
      case 2:
      if (ftype == Thrift.Type.STRUCT) {
        this.source = new ModelSpec();
        this.source.read(input);
      } else {
        input.skip(ftype);
      }
      break;
      case 3:
      if (ftype == Thrift.Type.STRUCT) {
        this.target = new ModelSpec();
        this.target.read(input);
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

CloudATL_launch_args.prototype.write = function(output) {
  output.writeStructBegin('CloudATL_launch_args');
  if (this.transformation !== null && this.transformation !== undefined) {
    output.writeFieldBegin('transformation', Thrift.Type.STRING, 1);
    output.writeString(this.transformation);
    output.writeFieldEnd();
  }
  if (this.source !== null && this.source !== undefined) {
    output.writeFieldBegin('source', Thrift.Type.STRUCT, 2);
    this.source.write(output);
    output.writeFieldEnd();
  }
  if (this.target !== null && this.target !== undefined) {
    output.writeFieldBegin('target', Thrift.Type.STRUCT, 3);
    this.target.write(output);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

CloudATL_launch_result = function(args) {
  this.success = null;
  this.err1 = null;
  this.err2 = null;
  if (args instanceof InvalidTransformation) {
    this.err1 = args;
    return;
  }
  if (args instanceof InvalidModelSpec) {
    this.err2 = args;
    return;
  }
  if (args) {
    if (args.success !== undefined && args.success !== null) {
      this.success = args.success;
    }
    if (args.err1 !== undefined && args.err1 !== null) {
      this.err1 = args.err1;
    }
    if (args.err2 !== undefined && args.err2 !== null) {
      this.err2 = args.err2;
    }
  }
};
CloudATL_launch_result.prototype = {};
CloudATL_launch_result.prototype.read = function(input) {
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
      if (ftype == Thrift.Type.STRING) {
        this.success = input.readString().value;
      } else {
        input.skip(ftype);
      }
      break;
      case 1:
      if (ftype == Thrift.Type.STRUCT) {
        this.err1 = new InvalidTransformation();
        this.err1.read(input);
      } else {
        input.skip(ftype);
      }
      break;
      case 2:
      if (ftype == Thrift.Type.STRUCT) {
        this.err2 = new InvalidModelSpec();
        this.err2.read(input);
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

CloudATL_launch_result.prototype.write = function(output) {
  output.writeStructBegin('CloudATL_launch_result');
  if (this.success !== null && this.success !== undefined) {
    output.writeFieldBegin('success', Thrift.Type.STRING, 0);
    output.writeString(this.success);
    output.writeFieldEnd();
  }
  if (this.err1 !== null && this.err1 !== undefined) {
    output.writeFieldBegin('err1', Thrift.Type.STRUCT, 1);
    this.err1.write(output);
    output.writeFieldEnd();
  }
  if (this.err2 !== null && this.err2 !== undefined) {
    output.writeFieldBegin('err2', Thrift.Type.STRUCT, 2);
    this.err2.write(output);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

CloudATL_getJobs_args = function(args) {
};
CloudATL_getJobs_args.prototype = {};
CloudATL_getJobs_args.prototype.read = function(input) {
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
    input.skip(ftype);
    input.readFieldEnd();
  }
  input.readStructEnd();
  return;
};

CloudATL_getJobs_args.prototype.write = function(output) {
  output.writeStructBegin('CloudATL_getJobs_args');
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

CloudATL_getJobs_result = function(args) {
  this.success = null;
  if (args) {
    if (args.success !== undefined && args.success !== null) {
      this.success = Thrift.copyList(args.success, [null]);
    }
  }
};
CloudATL_getJobs_result.prototype = {};
CloudATL_getJobs_result.prototype.read = function(input) {
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
      if (ftype == Thrift.Type.LIST) {
        var _size314 = 0;
        var _rtmp3318;
        this.success = [];
        var _etype317 = 0;
        _rtmp3318 = input.readListBegin();
        _etype317 = _rtmp3318.etype;
        _size314 = _rtmp3318.size;
        for (var _i319 = 0; _i319 < _size314; ++_i319)
        {
          var elem320 = null;
          elem320 = input.readString().value;
          this.success.push(elem320);
        }
        input.readListEnd();
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

CloudATL_getJobs_result.prototype.write = function(output) {
  output.writeStructBegin('CloudATL_getJobs_result');
  if (this.success !== null && this.success !== undefined) {
    output.writeFieldBegin('success', Thrift.Type.LIST, 0);
    output.writeListBegin(Thrift.Type.STRING, this.success.length);
    for (var iter321 in this.success)
    {
      if (this.success.hasOwnProperty(iter321))
      {
        iter321 = this.success[iter321];
        output.writeString(iter321);
      }
    }
    output.writeListEnd();
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

CloudATL_getStatus_args = function(args) {
  this.token = null;
  if (args) {
    if (args.token !== undefined && args.token !== null) {
      this.token = args.token;
    } else {
      throw new Thrift.TProtocolException(Thrift.TProtocolExceptionType.UNKNOWN, 'Required field token is unset!');
    }
  }
};
CloudATL_getStatus_args.prototype = {};
CloudATL_getStatus_args.prototype.read = function(input) {
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
        this.token = input.readString().value;
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

CloudATL_getStatus_args.prototype.write = function(output) {
  output.writeStructBegin('CloudATL_getStatus_args');
  if (this.token !== null && this.token !== undefined) {
    output.writeFieldBegin('token', Thrift.Type.STRING, 1);
    output.writeString(this.token);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

CloudATL_getStatus_result = function(args) {
  this.success = null;
  this.err1 = null;
  if (args instanceof TransformationTokenNotFound) {
    this.err1 = args;
    return;
  }
  if (args) {
    if (args.success !== undefined && args.success !== null) {
      this.success = new TransformationStatus(args.success);
    }
    if (args.err1 !== undefined && args.err1 !== null) {
      this.err1 = args.err1;
    }
  }
};
CloudATL_getStatus_result.prototype = {};
CloudATL_getStatus_result.prototype.read = function(input) {
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
      if (ftype == Thrift.Type.STRUCT) {
        this.success = new TransformationStatus();
        this.success.read(input);
      } else {
        input.skip(ftype);
      }
      break;
      case 1:
      if (ftype == Thrift.Type.STRUCT) {
        this.err1 = new TransformationTokenNotFound();
        this.err1.read(input);
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

CloudATL_getStatus_result.prototype.write = function(output) {
  output.writeStructBegin('CloudATL_getStatus_result');
  if (this.success !== null && this.success !== undefined) {
    output.writeFieldBegin('success', Thrift.Type.STRUCT, 0);
    this.success.write(output);
    output.writeFieldEnd();
  }
  if (this.err1 !== null && this.err1 !== undefined) {
    output.writeFieldBegin('err1', Thrift.Type.STRUCT, 1);
    this.err1.write(output);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

CloudATL_kill_args = function(args) {
  this.token = null;
  if (args) {
    if (args.token !== undefined && args.token !== null) {
      this.token = args.token;
    } else {
      throw new Thrift.TProtocolException(Thrift.TProtocolExceptionType.UNKNOWN, 'Required field token is unset!');
    }
  }
};
CloudATL_kill_args.prototype = {};
CloudATL_kill_args.prototype.read = function(input) {
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
        this.token = input.readString().value;
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

CloudATL_kill_args.prototype.write = function(output) {
  output.writeStructBegin('CloudATL_kill_args');
  if (this.token !== null && this.token !== undefined) {
    output.writeFieldBegin('token', Thrift.Type.STRING, 1);
    output.writeString(this.token);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

CloudATL_kill_result = function(args) {
  this.err1 = null;
  if (args instanceof TransformationTokenNotFound) {
    this.err1 = args;
    return;
  }
  if (args) {
    if (args.err1 !== undefined && args.err1 !== null) {
      this.err1 = args.err1;
    }
  }
};
CloudATL_kill_result.prototype = {};
CloudATL_kill_result.prototype.read = function(input) {
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
        this.err1 = new TransformationTokenNotFound();
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

CloudATL_kill_result.prototype.write = function(output) {
  output.writeStructBegin('CloudATL_kill_result');
  if (this.err1 !== null && this.err1 !== undefined) {
    output.writeFieldBegin('err1', Thrift.Type.STRUCT, 1);
    this.err1.write(output);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

CloudATLClient = function(input, output) {
    this.input = input;
    this.output = (!output) ? input : output;
    this.seqid = 0;
};
CloudATLClient.prototype = {};
CloudATLClient.prototype.launch = function(transformation, source, target, callback) {
  this.send_launch(transformation, source, target, callback); 
  if (!callback) {
    return this.recv_launch();
  }
};

CloudATLClient.prototype.send_launch = function(transformation, source, target, callback) {
  this.output.writeMessageBegin('launch', Thrift.MessageType.CALL, this.seqid);
  var args = new CloudATL_launch_args();
  args.transformation = transformation;
  args.source = source;
  args.target = target;
  args.write(this.output);
  this.output.writeMessageEnd();
  if (callback) {
    var self = this;
    this.output.getTransport().flush(true, function() {
      var result = null;
      try {
        result = self.recv_launch();
      } catch (e) {
        result = e;
      }
      callback(result);
    });
  } else {
    return this.output.getTransport().flush();
  }
};

CloudATLClient.prototype.recv_launch = function() {
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
  var result = new CloudATL_launch_result();
  result.read(this.input);
  this.input.readMessageEnd();

  if (null !== result.err1) {
    throw result.err1;
  }
  if (null !== result.err2) {
    throw result.err2;
  }
  if (null !== result.success) {
    return result.success;
  }
  throw 'launch failed: unknown result';
};
CloudATLClient.prototype.getJobs = function(callback) {
  this.send_getJobs(callback); 
  if (!callback) {
    return this.recv_getJobs();
  }
};

CloudATLClient.prototype.send_getJobs = function(callback) {
  this.output.writeMessageBegin('getJobs', Thrift.MessageType.CALL, this.seqid);
  var args = new CloudATL_getJobs_args();
  args.write(this.output);
  this.output.writeMessageEnd();
  if (callback) {
    var self = this;
    this.output.getTransport().flush(true, function() {
      var result = null;
      try {
        result = self.recv_getJobs();
      } catch (e) {
        result = e;
      }
      callback(result);
    });
  } else {
    return this.output.getTransport().flush();
  }
};

CloudATLClient.prototype.recv_getJobs = function() {
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
  var result = new CloudATL_getJobs_result();
  result.read(this.input);
  this.input.readMessageEnd();

  if (null !== result.success) {
    return result.success;
  }
  throw 'getJobs failed: unknown result';
};
CloudATLClient.prototype.getStatus = function(token, callback) {
  this.send_getStatus(token, callback); 
  if (!callback) {
    return this.recv_getStatus();
  }
};

CloudATLClient.prototype.send_getStatus = function(token, callback) {
  this.output.writeMessageBegin('getStatus', Thrift.MessageType.CALL, this.seqid);
  var args = new CloudATL_getStatus_args();
  args.token = token;
  args.write(this.output);
  this.output.writeMessageEnd();
  if (callback) {
    var self = this;
    this.output.getTransport().flush(true, function() {
      var result = null;
      try {
        result = self.recv_getStatus();
      } catch (e) {
        result = e;
      }
      callback(result);
    });
  } else {
    return this.output.getTransport().flush();
  }
};

CloudATLClient.prototype.recv_getStatus = function() {
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
  var result = new CloudATL_getStatus_result();
  result.read(this.input);
  this.input.readMessageEnd();

  if (null !== result.err1) {
    throw result.err1;
  }
  if (null !== result.success) {
    return result.success;
  }
  throw 'getStatus failed: unknown result';
};
CloudATLClient.prototype.kill = function(token, callback) {
  this.send_kill(token, callback); 
  if (!callback) {
  this.recv_kill();
  }
};

CloudATLClient.prototype.send_kill = function(token, callback) {
  this.output.writeMessageBegin('kill', Thrift.MessageType.CALL, this.seqid);
  var args = new CloudATL_kill_args();
  args.token = token;
  args.write(this.output);
  this.output.writeMessageEnd();
  if (callback) {
    var self = this;
    this.output.getTransport().flush(true, function() {
      var result = null;
      try {
        result = self.recv_kill();
      } catch (e) {
        result = e;
      }
      callback(result);
    });
  } else {
    return this.output.getTransport().flush();
  }
};

CloudATLClient.prototype.recv_kill = function() {
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
  var result = new CloudATL_kill_result();
  result.read(this.input);
  this.input.readMessageEnd();

  if (null !== result.err1) {
    throw result.err1;
  }
  return;
};
